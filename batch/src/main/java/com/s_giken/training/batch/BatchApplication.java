package com.s_giken.training.batch;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;


@SpringBootApplication
public class BatchApplication implements CommandLineRunner {
	private final Logger logger = LoggerFactory.getLogger(BatchApplication.class);
	private final JdbcTemplate jdbcTemplate;
	//挿入レコード数取得用変数
	int memberCount = 0;
	int chargeCount = 0;
	int count = 0;

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

	public BatchApplication(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void countRecord(LocalDate localDate) {
		//請求データ状況に該当の請求年月で確定状況がTRUEのレコード数をカウント
		int count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM T_BILLING_STATUS WHERE billing_ym = ? AND is_commit = TRUE",
				Integer.class,
				java.sql.Date.valueOf(localDate));
		this.count = count;
	}

	public void deleteBillingData(LocalDate localDate) {
		//請求明細データから該当する請求年月のレコードを削除
		jdbcTemplate.update("DELETE FROM T_BILLING_DETAIL_DATA WHERE billing_ym = ?",
				java.sql.Date.valueOf(localDate));

		//請求データから該当する請求年月のレコードを削除
		jdbcTemplate.update("DELETE FROM T_BILLING_DATA WHERE billing_ym = ?",
				java.sql.Date.valueOf(localDate));

		//請求データ状況から該当する請求年月のレコードを削除
		jdbcTemplate.update("DELETE FROM T_BILLING_STATUS WHERE billing_ym = ?",
				java.sql.Date.valueOf(localDate));
	}

	public void insertBillingStatus(LocalDate localDate) {
		//請求データ状況に入力された請求年月と確定状況falseのレコードを挿入
		jdbcTemplate.update(
				"INSERT INTO T_BILLING_STATUS(billing_ym, is_commit) VALUES (?, ?)",
				java.sql.Date.valueOf(localDate), false);
	}

	public void insertBillingDataAndDetailData(LocalDate localDate) {
		//chargeテーブルのリストを取得
		List<Map<String, Object>> charge = jdbcTemplate.queryForList(
				"SELECT * FROM T_CHARGE WHERE start_date <= ? AND (end_date IS NULL OR end_date >= ?) ",
				java.sql.Date.valueOf(localDate),
				java.sql.Date.valueOf(localDate));

		//memberテーブルのリストを取得
		List<Map<String, Object>> member = jdbcTemplate.queryForList(
				"SELECT * FROM T_MEMBER WHERE start_date <= ? AND (end_date IS NULL OR end_date >= ?) ",
				java.sql.Date.valueOf(localDate),
				java.sql.Date.valueOf(localDate));

		//料金合計のオブジェクト作成
		int total = jdbcTemplate.queryForObject(
				"SELECT SUM(amount) FROM T_CHARGE WHERE start_date <= ? AND (end_date IS NULL OR end_date >= ?) ",
				Integer.class, java.sql.Date.valueOf(localDate),
				java.sql.Date.valueOf(localDate));


		for (Map<String, Object> memberRow : member) {
			int memberId = (Integer) memberRow.get("member_id");
			String memberMail = (String) memberRow.get("mail");
			String memberName = (String) memberRow.get("name");
			String memberAddress = (String) memberRow.get("address");
			Timestamp memberStartDate = (Timestamp) memberRow.get("start_date");
			Timestamp memberEndDate = (Timestamp) memberRow.get("end_date");
			int paymentMethod = (Integer) memberRow.get("payment_method");

			//請求データを挿入
			int memberCounting = jdbcTemplate.update(
					"INSERT INTO T_BILLING_DATA(billing_ym, member_id, mail, name, address, start_date, end_date, payment_method, amount, tax_ratio, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					java.sql.Date.valueOf(localDate), memberId, memberMail, memberName,
					memberAddress, memberStartDate, memberEndDate, paymentMethod, total,
					0.1, (total + (total * 0.1)));
			memberCount += memberCounting;

			for (Map<String, Object> chargeRow : charge) {
				int chargeId = (Integer) chargeRow.get("charge_id");
				String chargeName = (String) chargeRow.get("name");
				int amount = (Integer) chargeRow.get("amount");
				Timestamp chargeStartDate = (Timestamp) chargeRow.get("start_date");
				Timestamp chargeEndDate = (Timestamp) chargeRow.get("end_date");

				//請求明細データを挿入
				int chargeCounting = jdbcTemplate.update(
						"INSERT INTO T_BILLING_DETAIL_DATA(billing_ym, member_id, charge_id, name, amount, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?)",
						java.sql.Date.valueOf(localDate), memberId, chargeId, chargeName,
						amount, chargeStartDate, chargeEndDate);
				chargeCount += chargeCounting;
			}
		}
	}

	@Override
	@Transactional
	public void run(String... args) throws RuntimeException {
		logger.info("-".repeat(40));
		String inputDate = args[0];
		String year = inputDate.substring(0, 4);
		String month = inputDate.substring(4);
		int yearNum = Integer.parseInt(year);
		int monthNum = Integer.parseInt(month);


		//入力値の条件指定
		if (args.length != 1 || !args[0].matches("^\\d{6}$")) {
			logger.info("正しい形式で年月を入力してください。");
			return;
		} else if (monthNum > 12 || monthNum < 1) {
			logger.info("正しい形式で年月を入力してください。");
			return;
		}

		try {
			//LocalDate型にキャスト
			LocalDate localDate = LocalDate.of(yearNum, monthNum, 1);
			countRecord(localDate);

			if (count > 0) {
				logger.info("指定された年月の請求情報は既に存在します。");
				return;
			}

			logger.info(year + "年" + month + "月分の請求情報を確認しています。");

			deleteBillingData(localDate);
			logger.info("データベースから" + year + "年" + month + "月分の未確定請求情報を削除しました.");

			logger.info(year + "年" + month + "月分の請求ステータス情報を追加しています。");

			insertBillingStatus(localDate);
			logger.info("1件追加しました。");

			logger.info(year + "年" + month + "月分の請求データ情報を追加しています。");

			insertBillingDataAndDetailData(localDate);

			//請求データの挿入レコード数
			logger.info(memberCount + "件追加しました。");
			logger.info(year + "年" + month + "月分の請求明細データ情報を追加しています。");
			//請求明細データの挿入レコード数
			logger.info(chargeCount + "件追加しました。");
		} catch (DataAccessException e) {
			logger.error("データベースアクセス中にエラーが発生しました。");
		} catch (NullPointerException e) {
			logger.error("料金情報が存在しませんでした。");
		}
		logger.info("-".repeat(40));
	}
}
