package com.s_giken.training.webapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.s_giken.training.webapp.model.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    /*
     * SprintBootでは、JpaRepositioyを継承したインターフェースを元に実態クラスが自動生成されます。
     * 
     * 【予め用意されているメソッド】
     * <S extends T> S save(S entity); // エンティティの保存
     * T findOne(ID primaryKey); // 主キーを元にエンティティを取得
     * Iterable<T> findAll(); // 全てのエンティティを取得
     * Long count(); // エンティティの件数を取得
     * void delete(T entity); // エンティティの削除
     * boolean exists(ID primaryKey); // 主キーを元にエンティティが存在するか確認
     * 
     * 別途メソッドを定義する場合はルールに従ってメソッド名を定義する必要があります。
     * 詳細は以下の参考先URLを確認して下さい。
     * https://docs.spring.io/spring-data/jpa/docs/1.11.1.RELEASE/reference/html/# jpa.query-methods
     */

    // TODO: 名前でも検索できるようにメソッドを定義しなおす
    public List<Member> findByMailLikeAndNameLike(String mail, String name);
}
