package com.s_giken.training.webapp.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.s_giken.training.webapp.model.entity.Charge;
import com.s_giken.training.webapp.model.entity.ChargeSearchCondition;
import com.s_giken.training.webapp.repository.ChargeRepository;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * 加入者管理機能のサービスクラス(実態クラス)
 */
@Service
public class ChargeServiceImpl implements ChargeService {
    private ChargeRepository chargeRepository;

    /**
     * 加入者管理機能のサービスクラスのコンストラクタ
     * 
     * @param chargeRepository 加入者管理機能のリポジトリクラス(SpringのDIコンテナから渡される)
     */
    public ChargeServiceImpl(ChargeRepository chargeRepository) {
        this.chargeRepository = chargeRepository;
    }

    /**
     * 加入者を全件取得する
     * 
     * @return 全加入者情報
     */
    @Override
    public List<Charge> findAll() {
        return chargeRepository.findAll();
    }

    /**
     * 加入者を1件取得する
     * 
     * @param chargeId 加入者ID
     * @return 加入者IDに一致した加入者情報
     */
    @Override
    public Optional<Charge> findById(int chargeId) {
        return chargeRepository.findById(chargeId);
    }

    /**
     * 加入者を条件検索する
     * 
     * @param chargeSearchCondition 加入者検索条件
     * @return 条件に一致した加入者情報
     */
    @Override
    public List<Charge> findByConditions(ChargeSearchCondition chargeSearchCondition) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(chargeSearchCondition.getSortDirection()),
                chargeSearchCondition.getColumn());

        return chargeRepository.findByNameLike("%" + chargeSearchCondition.getName() + "%",
                sort);
    }

    @Override
    public List<Charge> findChargesForMonth(String year, String month) {
        var chargeSearchCondition = chargeRepository.findAll();
        List<Charge> chargeList = new ArrayList<>();
        for (Charge charge : chargeSearchCondition) {
            int date = Integer.parseInt(year + month);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            Date beforeFormatStartDate = charge.getStartDate();
            String formatStartDate = sdf.format(beforeFormatStartDate);
            int startDate = Integer.parseInt(formatStartDate);

            Date beforeFormatEndDate = charge.getEndDate();
            String formatEndDate = null;
            int endDate = 0;
            if (beforeFormatEndDate != null) {
                formatEndDate = sdf.format(beforeFormatEndDate);
            }
            if (formatEndDate != null) {
                endDate = Integer.parseInt(formatEndDate);
            }
            if (startDate <= date && (endDate == 0 || endDate >= date)) {
                chargeList.add(charge);
            }
        }
        return chargeList;
    }

    @Override
    public List<Charge> findChargesForNextMonth(String year, String month) {
        var chargeSearchCondition = chargeRepository.findAll();
        List<Charge> nextChargeList = new ArrayList<>();

        for (Charge charge : chargeSearchCondition) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            Date beforeFormatStartDate = charge.getStartDate();
            String formatStartDate = sdf.format(beforeFormatStartDate);
            int startDate = Integer.parseInt(formatStartDate);
            Date beforeFormatEndDate = charge.getEndDate();
            String formatEndDate = null;
            int endDate = 0;

            int nextYear = Integer.parseInt(year);
            int nextMonth = Integer.parseInt(month) + 1;
            if (nextMonth > 12) {
                nextMonth = 1;
                nextYear++;
            }
            String nextYearStr = String.valueOf(nextYear);
            String nextMonthStr = String.format("%02d", nextMonth);
            int nextDate = Integer.parseInt(nextYearStr + nextMonthStr);

            if (beforeFormatEndDate != null) {
                formatEndDate = sdf.format(beforeFormatEndDate);
            }
            if (formatEndDate != null) {
                endDate = Integer.parseInt(formatEndDate);
            }
            if (startDate <= nextDate && (endDate == 0 || endDate >= nextDate)) {
                nextChargeList.add(charge);
            }
        }

        return nextChargeList;
    }

    /**
     * 加入者を登録する
     *
     * @param charge 登録する加入者情報
     * @return 登録した加入者情報
     */
    @Override
    public void save(Charge charge) {
        chargeRepository.save(charge);
    }

    /**
     * 加入者を更新する
     * 
     * @param charge 更新する加入者情報
     * @return 更新した加入者情報
     */
    @Override
    public void deleteById(int chargeId) {
        chargeRepository.deleteById(chargeId);
    }
}
