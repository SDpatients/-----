package com.supplier.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.logistics.entity.ReceiptRecord;
import com.supplier.logistics.mapper.ReceiptRecordMapper;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.settlement.dto.ThreeWayMatchDTO;
import com.supplier.settlement.entity.Invoice;
import com.supplier.settlement.entity.ThreeWayMatch;
import com.supplier.settlement.mapper.InvoiceMapper;
import com.supplier.settlement.mapper.ThreeWayMatchMapper;
import com.supplier.settlement.service.ThreeWayMatchService;
import com.supplier.settlement.vo.ThreeWayMatchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 三单匹配服务实现（联动7: 自动匹配订单/收货/发票）
 * <p>
 * 跨模块联动: order + logistics + settlement
 * 匹配规则: 订单金额 >= 收货金额 >= 发票金额
 */
@Service
@RequiredArgsConstructor
public class ThreeWayMatchServiceImpl implements ThreeWayMatchService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final ReceiptRecordMapper receiptRecordMapper;
    private final InvoiceMapper invoiceMapper;
    private final ThreeWayMatchMapper threeWayMatchMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ThreeWayMatchVO> execute(ThreeWayMatchDTO dto) {
        Long supplierId = dto.getSupplierId();
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        BigDecimal tolerance = dto.getTolerance() != null ? dto.getTolerance() : BigDecimal.ZERO;

        // 1) 查询该供应商已确认的采购订单
        LambdaQueryWrapper<PurchaseOrder> orderWrapper = new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getSupplierId, supplierId)
                .eq(PurchaseOrder::getOrderStatus, 2); // 已确认
        if (dto.getOrderNo() != null && !dto.getOrderNo().isBlank()) {
            orderWrapper.eq(PurchaseOrder::getOrderNo, dto.getOrderNo());
        }
        if (startDate != null && endDate != null) {
            orderWrapper.ge(PurchaseOrder::getOrderDate, startDate)
                       .le(PurchaseOrder::getOrderDate, endDate);
        }
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(orderWrapper);

        // 2) 查询该供应商已确认的收货记录
        LambdaQueryWrapper<ReceiptRecord> receiptWrapper = new LambdaQueryWrapper<ReceiptRecord>()
                .eq(ReceiptRecord::getSupplierId, supplierId)
                .eq(ReceiptRecord::getReceiptStatus, 1); // 已确认
        if (startDate != null && endDate != null) {
            receiptWrapper.ge(ReceiptRecord::getReceiptTime, startDate.atStartOfDay())
                          .le(ReceiptRecord::getReceiptTime, endDate.plusDays(1).atStartOfDay());
        }
        List<ReceiptRecord> receipts = receiptRecordMapper.selectList(receiptWrapper);

        // 3) 查询该供应商已认证的发票
        LambdaQueryWrapper<Invoice> invoiceWrapper = new LambdaQueryWrapper<Invoice>()
                .eq(Invoice::getSupplierId, supplierId)
                .eq(Invoice::getInvoiceStatus, 3); // 已认证
        if (startDate != null && endDate != null) {
            invoiceWrapper.ge(Invoice::getInvoiceDate, startDate)
                          .le(Invoice::getInvoiceDate, endDate);
        }
        List<Invoice> invoices = invoiceMapper.selectList(invoiceWrapper);

        // 4) 执行匹配
        List<ThreeWayMatchVO> results = new ArrayList<>();
        BigDecimal totalOrderAmount = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalReceiptAmount = receipts.stream()
                .map(r -> r.getReceiptQty() != null ? r.getReceiptQty() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInvoiceAmount = invoices.stream()
                .map(i -> i.getInvoiceAmount() != null ? i.getInvoiceAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 为每个订单生成一条匹配记录（简化: 按总额匹配）
        if (!orders.isEmpty()) {
            PurchaseOrder firstOrder = orders.get(0);
            ThreeWayMatch match = new ThreeWayMatch();
            match.setOrderId(firstOrder.getId());
            match.setOrderNo(firstOrder.getOrderNo());
            match.setOrderAmount(totalOrderAmount);
            match.setReceiptAmount(totalReceiptAmount);
            match.setInvoiceAmount(totalInvoiceAmount);
            match.setSupplierId(supplierId);

            // 匹配判断: 订单 >= 收货 >= 发票
            int matchResult;
            String diffReason = null;
            BigDecimal diff = totalOrderAmount.subtract(totalInvoiceAmount);

            if (totalOrderAmount.compareTo(totalReceiptAmount.add(tolerance)) >= 0
                    && totalReceiptAmount.compareTo(totalInvoiceAmount.add(tolerance)) >= 0) {
                if (diff.abs().compareTo(tolerance) <= 0) {
                    matchResult = 0; // 完全匹配
                } else {
                    matchResult = 1; // 部分匹配
                    diffReason = "订单金额" + totalOrderAmount + " > 发票金额" + totalInvoiceAmount + "，差异" + diff;
                }
            } else {
                matchResult = 2; // 不匹配
                diffReason = buildMismatchReason(totalOrderAmount, totalReceiptAmount, totalInvoiceAmount);
            }

            match.setMatchResult(matchResult);
            match.setDiffAmount(diff);
            match.setDiffReason(diffReason);
            match.setMatchTime(LocalDateTime.now());
            match.setRemark("三单匹配自动执行");

            // 关联发票
            if (!invoices.isEmpty()) {
                match.setInvoiceId(invoices.get(0).getId());
                match.setInvoiceNo(invoices.get(0).getInvoiceNo());
            }
            threeWayMatchMapper.insert(match);

            ThreeWayMatchVO vo = toVO(match);
            results.add(vo);
        } else if (!invoices.isEmpty()) {
            // 无订单但有发票：不匹配
            ThreeWayMatch match = new ThreeWayMatch();
            match.setOrderAmount(BigDecimal.ZERO);
            match.setReceiptAmount(totalReceiptAmount);
            match.setSupplierId(supplierId);
            match.setMatchResult(2); // 不匹配
            match.setDiffReason("未找到对应采购订单");
            match.setMatchTime(LocalDateTime.now());
            if (!invoices.isEmpty()) {
                match.setInvoiceId(invoices.get(0).getId());
                match.setInvoiceNo(invoices.get(0).getInvoiceNo());
            }
            match.setInvoiceAmount(totalInvoiceAmount);
            threeWayMatchMapper.insert(match);
            results.add(toVO(match));
        }

        return results;
    }

    private String buildMismatchReason(BigDecimal orderAmt, BigDecimal receiptAmt, BigDecimal invoiceAmt) {
        StringBuilder sb = new StringBuilder("三单不匹配: ");
        if (orderAmt.compareTo(receiptAmt) < 0) {
            sb.append("收货金额(").append(receiptAmt).append(")超过订单金额(").append(orderAmt).append("); ");
        }
        if (receiptAmt.compareTo(invoiceAmt) < 0) {
            sb.append("发票金额(").append(invoiceAmt).append(")超过收货金额(").append(receiptAmt).append("); ");
        }
        return sb.toString();
    }

    private ThreeWayMatchVO toVO(ThreeWayMatch e) {
        ThreeWayMatchVO vo = new ThreeWayMatchVO();
        vo.setId(e.getId());
        vo.setOrderId(e.getOrderId());
        vo.setOrderNo(e.getOrderNo());
        vo.setOrderAmount(e.getOrderAmount());
        vo.setReceiptId(e.getReceiptId());
        vo.setReceiptAmount(e.getReceiptAmount());
        vo.setInvoiceId(e.getInvoiceId());
        vo.setInvoiceNo(e.getInvoiceNo());
        vo.setInvoiceAmount(e.getInvoiceAmount());
        vo.setSupplierId(e.getSupplierId());
        vo.setMatchResult(e.getMatchResult());
        vo.setMatchResultDesc(switch (e.getMatchResult() != null ? e.getMatchResult() : -1) {
            case 0 -> "完全匹配";
            case 1 -> "部分匹配";
            case 2 -> "不匹配";
            default -> "未知";
        });
        vo.setDiffAmount(e.getDiffAmount());
        vo.setDiffReason(e.getDiffReason());
        vo.setMatchTime(e.getMatchTime());
        vo.setRemark(e.getRemark());
        return vo;
    }
}