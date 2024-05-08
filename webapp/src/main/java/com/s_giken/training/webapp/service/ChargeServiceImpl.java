package com.s_giken.training.webapp.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.s_giken.training.webapp.model.entity.Charge;
import com.s_giken.training.webapp.model.entity.ChargeSearchCondition;
import com.s_giken.training.webapp.repository.ChargeRepository;

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
        if (chargeSearchCondition == null) {
            return chargeRepository.findByNameLike("%" + chargeSearchCondition.getName() + "%");
        } else {

            Sort sort = Sort.by(
                    Sort.Direction.fromString(chargeSearchCondition.getSortDirection()),
                    chargeSearchCondition.getColumn());

            return chargeRepository.findByNameLike("%" + chargeSearchCondition.getName() + "%",
                    sort);
        }
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
