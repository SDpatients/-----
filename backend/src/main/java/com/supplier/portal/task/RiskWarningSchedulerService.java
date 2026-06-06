package com.supplier.portal.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.logistics.entity.DeliveryNotice;
import com.supplier.logistics.mapper.DeliveryNoticeMapper;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.service.PortalTodoService;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.mapper.RfqMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险预警定时任务
 * 负责主动扫描订单逾期、送货逾期等风险，并生成通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskWarningSchedulerService {
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final DeliveryNoticeMapper deliveryNoticeMapper;
    private final MessageNoticeService messageNoticeService;
    private final PortalTodoService portalTodoService;
    private final RfqMapper rfqMapper;

    /**
     * 每日 08:00 扫描逾期风险并生成预警通知
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scanOverdueRisks() {
        log.info("[风险预警] 开始扫描逾期风险");
        int noticeCount = 0;

        // 1. 扫描待确认订单（订单状态=1 待确认且下发超过2天）
        noticeCount += scanPendingOrders();

        // 2. 扫描计划送货已逾期（计划送货日期 < 今天，实际未发货，状态<4）
        noticeCount += scanOverdueDeliveries();

        // 3. 扫描即将逾期送货（计划送货日期在未来2天内，实际未发货）
        noticeCount += scanApproachingDeliveries();

        // 4. 扫描逾期未完成的订单（交期已过，订单状态非完成/取消）
        noticeCount += scanOverdueOrders();

        // 5. 扫描即将截止的询价单
        noticeCount += scanApproachingRfqDeadline();

        log.info("[风险预警] 扫描完成，共生成 {} 条预警通知", noticeCount);
    }

    private int scanPendingOrders() {
        LocalDate threshold = LocalDate.now().minusDays(2);
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getOrderStatus, 1)
                        .le(PurchaseOrder::getCreateTime, threshold.atStartOfDay())
                        .select(PurchaseOrder::getId, PurchaseOrder::getOrderNo, PurchaseOrder::getSupplierId)
        );
        for (PurchaseOrder order : orders) {
            sendNotice(order.getSupplierId(), "订单待确认提醒",
                    String.format("订单 %s 待确认已超过2天，请尽快处理", order.getOrderNo()),
                    "order_pending", order.getId());
        }
        return orders.size();
    }

    private int scanOverdueDeliveries() {
        List<DeliveryNotice> notices = deliveryNoticeMapper.selectList(
                new LambdaQueryWrapper<DeliveryNotice>()
                        .lt(DeliveryNotice::getPlanDeliveryDate, LocalDate.now())
                        .isNull(DeliveryNotice::getActualDeliveryDate)
                        .lt(DeliveryNotice::getDeliveryStatus, 4)
                        .select(DeliveryNotice::getId, DeliveryNotice::getNoticeNo, DeliveryNotice::getSupplierId, DeliveryNotice::getPlanDeliveryDate)
        );
        for (DeliveryNotice notice : notices) {
            sendNotice(notice.getSupplierId(), "送货逾期预警",
                    String.format("送货单 %s 计划送货日期 %s 已逾期，请尽快安排发货",
                            notice.getNoticeNo(), notice.getPlanDeliveryDate()),
                    "delivery_delay", notice.getId());
        }
        return notices.size();
    }

    private int scanApproachingDeliveries() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);
        List<DeliveryNotice> notices = deliveryNoticeMapper.selectList(
                new LambdaQueryWrapper<DeliveryNotice>()
                        .ge(DeliveryNotice::getPlanDeliveryDate, start)
                        .le(DeliveryNotice::getPlanDeliveryDate, end)
                        .isNull(DeliveryNotice::getActualDeliveryDate)
                        .lt(DeliveryNotice::getDeliveryStatus, 4)
                        .select(DeliveryNotice::getId, DeliveryNotice::getNoticeNo, DeliveryNotice::getSupplierId, DeliveryNotice::getPlanDeliveryDate)
        );
        for (DeliveryNotice notice : notices) {
            sendNotice(notice.getSupplierId(), "送货即将到期提醒",
                    String.format("送货单 %s 计划送货日期 %s 即将到期，请安排好发货",
                            notice.getNoticeNo(), notice.getPlanDeliveryDate()),
                    "delivery_approaching", notice.getId());
        }
        return notices.size();
    }

    private int scanOverdueOrders() {
        // 扫描交期已过期但未完成的订单（状态: 草稿0/待确认1/已确认2/部分发货3）
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .lt(PurchaseOrder::getDeliveryDate, LocalDate.now())
                        .in(PurchaseOrder::getOrderStatus, 0, 1, 2, 3)
                        .select(PurchaseOrder::getId, PurchaseOrder::getOrderNo, PurchaseOrder::getSupplierId, PurchaseOrder::getDeliveryDate)
        );
        for (PurchaseOrder order : orders) {
            sendNotice(order.getSupplierId(), "订单逾期预警",
                    String.format("采购订单 %s 交期 %s 已逾期，请关注处理",
                            order.getOrderNo(), order.getDeliveryDate()),
                    "order_overdue", order.getId());
        }
        return orders.size();
    }

    private void sendNotice(Long supplierId, String title, String content, String businessType, Long businessId) {
        try {
            if (supplierId == null) {
                log.warn("[风险预警] 跳过无供应商的通知: title={}", title);
                return;
            }
            MessageNoticeCreateDTO dto = new MessageNoticeCreateDTO();
            dto.setReceiverSupplierId(supplierId);
            dto.setTitle(title);
            dto.setContent(content);
            dto.setBusinessType(businessType);
            dto.setBusinessId(businessId);
            dto.setChannel(1); // 站内信
            messageNoticeService.create(dto);
        } catch (Exception e) {
            log.error("[风险预警] 发送通知失败: supplierId={}, title={}", supplierId, title, e);
        }
    }

    /**
     * 扫描即将截止的询价单（报价截止日期在未来3天内），为采购方创建待办
     */
    private int scanApproachingRfqDeadline() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(3);
        List<Rfq> rfqs = rfqMapper.selectList(
                new LambdaQueryWrapper<Rfq>()
                        .in(Rfq::getRfqStatus, 1, 2)
                        .ge(Rfq::getQuoteDeadline, start)
                        .le(Rfq::getQuoteDeadline, end)
                        .select(Rfq::getId, Rfq::getRfqNo, Rfq::getQuoteDeadline)
        );
        for (Rfq rfq : rfqs) {
            try {
                PortalTodoCreateDTO todoDto = new PortalTodoCreateDTO();
                todoDto.setTodoType("rfq_deadline");
                todoDto.setBusinessType("rfq");
                todoDto.setBusinessId(rfq.getId());
                todoDto.setBusinessNo(rfq.getRfqNo());
                todoDto.setTitle("询价单即将截止 " + rfq.getRfqNo());
                todoDto.setDueTime(rfq.getQuoteDeadline() != null ? rfq.getQuoteDeadline() : LocalDateTime.now().plusDays(1));
                portalTodoService.create(todoDto);
            } catch (Exception e) {
                log.error("[风险预警] 创建询价截止待办失败: rfqId={}", rfq.getId(), e);
            }
        }
        return rfqs.size();
    }
}