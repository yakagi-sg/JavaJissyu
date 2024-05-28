package com.s_giken.training.webapp.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.s_giken.training.webapp.model.entity.Member;
import com.s_giken.training.webapp.model.entity.MemberSearchCondition;
import com.s_giken.training.webapp.repository.MemberRepository;

/**
 * 加入者管理機能のサービスクラス(実態クラス)
 */
@Service
public class MemberServiceImpl implements MemberService {
    private MemberRepository memberRepository;

    /**
     * 加入者管理機能のサービスクラスのコンストラクタ
     * 
     * @param memberRepository 加入者管理機能のリポジトリクラス(SpringのDIコンテナから渡される)
     */
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 加入者を全件取得する
     * 
     * @return 全加入者情報
     */
    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    /**
     * 加入者を1件取得する
     * 
     * @param memberId 加入者ID
     * @return 加入者IDに一致した加入者情報
     */
    @Override
    public Optional<Member> findById(int memberId) {
        return memberRepository.findById(memberId);
    }

    /**
     * 加入者を条件検索する
     * 
     * @param memberSearchCondition 加入者検索条件
     * @return 条件に一致した加入者情報
     */
    @Override
    public List<Member> findByConditions(MemberSearchCondition memberSearchCondition) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(memberSearchCondition.getSortDirection()),
                memberSearchCondition.getColumn());

        return memberRepository.findByMailLikeAndNameLike(
                "%" + memberSearchCondition.getMail() + "%",
                "%" + memberSearchCondition.getName() + "%",
                sort);
    }



    /**
     * 加入者を登録する
     *
     * @param member 登録する加入者情報
     * @return 登録した加入者情報
     */
    @Override
    public void save(Member member) {
        memberRepository.save(member);
    }

    /**
     * 加入者を更新する
     * 
     * @param member 更新する加入者情報
     * @return 更新した加入者情報
     */
    @Override
    public void deleteById(int memberId) {
        memberRepository.deleteById(memberId);
    }

    @Override
    public void saveMemberNum(Member member) {
        Integer maxMemberNum = memberRepository.findMaxMemberNum();
        if (maxMemberNum == null) {
            maxMemberNum = 1;
            member.setMemberNum(maxMemberNum);
            memberRepository.save(member);
        }
        if(maxMemberNum != null){
        Integer nextMemberNum = maxMemberNum + 1;
        member.setMemberNum(nextMemberNum);
        memberRepository.save(member);
        }
    }
}
