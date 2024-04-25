package com.s_giken.training.webapp.model.entity;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 【アノテーションのヒント】
 * 
 * @Entity ：エンティティクラスであることを示す。
 * @EntityListeners ：エンティティクラスの変更を監視するリスナークラスを指定する。
 * @Table ：対応するデータベーステーブル名を指定する。
 * @Data ：メンバー変数に対するゲッター・セッターを自動生成する。
 * @NoArgsConstructor ：引数のないコンストラクタを自動生成する。
 * @AllArgsConstructor ：全てのメンバー変数を引数に持つコンストラクタを自動生成する。
 * @Id ：主キーであることを示す。
 * @GeneratedValue ：主キーの採番方法を指定する。
 * @Column ：対応する列名を指定する。
 * @NotNull ：null不可であることを示す。
 * @NotBlank ：空文字不可であることを示す。
 * @DateTimeFormat ：日付のフォーマットを指定する。
 * @CreatedDate ：作成日時を自動設定する。
 * @LastModifiedDate ：更新日時を自動設定する。
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "T_CHARGE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Charge {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "charge_id")
    private int chargeId;

    @Column(name = "name")
    @NotNull
    @NotBlank
    private String name;

    @Column(name = "amount")
    @NotNull
    private int amount;

    @Column(name = "start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private Date startDate;

    @Column(name = "end_date")
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date endDate;


    @CreatedDate
    private Timestamp createdDate;

    @LastModifiedDate
    private Timestamp updatedDate;
}


