package com.supplier.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.quality.converter.QualityAppealConverter;
import com.supplier.quality.dto.QualityAppealAuditDTO;
import com.supplier.quality.dto.QualityAppealCreateDTO;
import com.supplier.quality.entity.QualityAppeal;
import com.supplier.quality.mapper.QualityAppealMapper;
import com.supplier.quality.query.QualityAppealQuery;
import com.supplier.quality.service.QualityAppealService;
import com.supplier.quality.vo.QualityAppealVO;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class QualityAppealServiceImpl implements QualityAppealService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_SUBMITTED = 1;
    private static final int STATUS_AUDITING = 2;
    private static final int STATUS_APPROVED = 3;
    private static final int STATUS_REJECTED = 4;

    private final QualityAppealMapper qualityAppealMapper;

    @Override
    public PageResult<QualityAppealVO> page(QualityAppealQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<QualityAppeal> wrapper = new LambdaQueryWrapper<QualityAppeal>()
                .eq(supplierId != null, QualityAppeal::getSupplierId, supplierId)
                .eq(query.getNcrId() != null, QualityAppeal::getNcrId, query.getNcrId())
                .eq(query.getAppealStatus() != null, QualityAppeal::getAppealStatus, query.getAppealStatus())
                .ge(query.getStartDate() != null, QualityAppeal::getCreateTime, query.getStartDate() != null ? query.getStartDate().atStartOfDay() : null)
                .le(query.getEndDate() != null, QualityAppeal::getCreateTime, query.getEndDate() != null ? query.getEndDate().plusDays(1).atStartOfDay() : null)
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(QualityAppeal::getAppealNo, query.getKeyword())
                        .or()
                        .like(QualityAppeal::getAppealReason, query.getKeyword()))
                .orderByDesc(QualityAppeal::getCreateTime);
        Page<QualityAppeal> page = qualityAppealMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(QualityAppealConverter::toVO));
    }

    @Override
    public QualityAppealVO getDetail(Long id) {
        QualityAppeal appeal = getWithScope(id);
        return QualityAppealConverter.toVO(appeal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(QualityAppealCreateDTO dto) {
        QualityAppeal entity = QualityAppealConverter.toEntity(dto);
        entity.setAppealNo(generateAppealNo());
        qualityAppealMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        QualityAppeal appeal = getWithScope(id);
        if (!Integer.valueOf(STATUS_DRAFT).equals(appeal.getAppealStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态的申诉可以提交");
        }
        appeal.setAppealStatus(STATUS_SUBMITTED);
        appeal.setSubmitTime(LocalDateTime.now());
        qualityAppealMapper.updateById(appeal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, QualityAppealAuditDTO dto) {
        QualityAppeal appeal = getWithScope(id);
        if (!Integer.valueOf(STATUS_SUBMITTED).equals(appeal.getAppealStatus()) && !Integer.valueOf(STATUS_AUDITING).equals(appeal.getAppealStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交或审核中状态的申诉可以审核通过");
        }
        appeal.setAppealStatus(STATUS_APPROVED);
        appeal.setAuditBy(SecurityUtils.getUserId());
        appeal.setAuditTime(LocalDateTime.now());
        appeal.setAuditRemark(dto.getAuditRemark());
        qualityAppealMapper.updateById(appeal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, QualityAppealAuditDTO dto) {
        QualityAppeal appeal = getWithScope(id);
        if (!Integer.valueOf(STATUS_SUBMITTED).equals(appeal.getAppealStatus()) && !Integer.valueOf(STATUS_AUDITING).equals(appeal.getAppealStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交或审核中状态的申诉可以被驳回");
        }
        appeal.setAppealStatus(STATUS_REJECTED);
        appeal.setAuditBy(SecurityUtils.getUserId());
        appeal.setAuditTime(LocalDateTime.now());
        appeal.setAuditRemark(dto.getAuditRemark());
        qualityAppealMapper.updateById(appeal);
    }

    private String generateAppealNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "AP" + datePart;
        Long count = qualityAppealMapper.selectCount(
                new LambdaQueryWrapper<QualityAppeal>()
                        .likeRight(QualityAppeal::getAppealNo, prefix));
        return prefix + String.format("%04d", (count == null ? 0 : count) + 1);
    }

    private QualityAppeal getWithScope(Long id) {
        QualityAppeal appeal = qualityAppealMapper.selectById(id);
        if (appeal == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && (appeal.getSupplierId() == null || !appeal.getSupplierId().equals(SecurityUtils.getSupplierId()))) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return appeal;
    }

    private Long resolveSupplierId(Long querySupplierId) {
        if (!SecurityUtils.isSupplierUser()) {
            return querySupplierId;
        }
        Long supplierId = SecurityUtils.getSupplierId();
        if (supplierId == null) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN.getCode(), "供应商用户未绑定供应商");
        }
        if (querySupplierId != null && !supplierId.equals(querySupplierId)) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return supplierId;
    }
}