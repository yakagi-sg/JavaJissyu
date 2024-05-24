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
import java.sql.Date;
import java.time.DateTimeException;


@SpringBootApplication
public class BatchApplication implements CommandLineRunner {
	private final Logger logger = LoggerFactory.getLogger(BatchApplication.class);
	private final JdbcTemplate jdbcTemplate;


	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

	public BatchApplication(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public int countBillingStatusRecord(Date date) {
		//請求データ状況に該当の請求年月で確定状況がTRUEのレコード数をカウント
		int count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM T_BILLING_STATUS WHERE billing_ym = ? AND is_commit = TRUE",
				Integer.class, date);
		return count;
	}

	public void deleteBillingData(Date date) {
		//請求明細データから該当する請求年月のレコードを削除
		jdbcTemplate.update("DELETE FROM T_BILLING_DETAIL_DATA WHERE billing_ym = ?",
				date);

		//請求データから該当する請求年月のレコードを削除
		jdbcTemplate.update("DELETE FROM T_BILLING_DATA WHERE billing_ym = ?",
				date);

		//請求データ状況から該当する請求年月のレコードを削除
		jdbcTemplate.update("DELETE FROM T_BILLING_STATUS WHERE billing_ym = ?",
				date);
	}

	public int insertBillingStatus(Date date) {
		//請求データ状況に入力された請求年月と確定状況falseのレコードを挿入
		int statusCount = jdbcTemplate.update(
				"INSERT INTO T_BILLING_STATUS(billing_ym, is_commit) VALUES (?, ?)",
				date, false);
		return statusCount;
	}

	public class countInsertBillingDataAndDetailData {
		private int memberCount;
		private int chargeCount;

		public countInsertBillingDataAndDetailData(int memberCount, int chargeCount) {
			this.memberCount = memberCount;
			this.chargeCount = chargeCount;

		}
	}

	public countInsertBillingDataAndDetailData insertBillingDataAndDetailData(Date date,
			Date lastDate) {
		int memberCount = 0;
		int chargeCount = 0;

		//chargeテーブルのリストを取得
		List<Map<String, Object>> charge = jdbcTemplate.queryForList(
				"SELECT * FROM T_CHARGE WHERE start_date <= ? AND (end_date IS NULL OR end_date >= ?) ",
				lastDate, date);

		//memberテーブルのリストを取得
		List<Map<String, Object>> member = jdbcTemplate.queryForList(
				"SELECT * FROM T_MEMBER WHERE start_date <= ? AND (end_date IS NULL OR end_date >= ?) ",
				lastDate, date);

		//料金合計のオブジェクト作成
		int total = jdbcTemplate.queryForObject(
				"SELECT SUM(amount) FROM T_CHARGE WHERE start_date <= ? AND (end_date IS NULL OR end_date >= ?) ",
				Integer.class, lastDate, date);


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
					date, memberId, memberMail, memberName,
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
						date, memberId, chargeId, chargeName,
						amount, chargeStartDate, chargeEndDate);
				chargeCount += chargeCounting;
			}
		}
		return new countInsertBillingDataAndDetailData(memberCount, chargeCount);
	}

	@Override
	@Transactional
	public void run(String... args) throws RuntimeException {
		logger.info("-".repeat(40));
		String inputDate = args[0];
		String year = inputDate.substring(0, 4);
		String month = inputDate.substring(4);
		int yearNum = 0;
		int monthNum = 0;

		try {
			yearNum = Integer.parseInt(year);
			monthNum = Integer.parseInt(month);
		} catch (NumberFormatException e) {
			logger.error("数字をyyyyMMの形式で入力してください。");
			logger.info("-".repeat(40));
			return;
		}

		//入力値の条件指定 
		if (args.length != 1 || !args[0].matches("^\\d{6}$")) {
			logger.info("yyyyMMの形式で年月を入力してください。");
			logger.info("-".repeat(40));
			return;
		}
		try {
			LocalDate firstLocalDate = LocalDate.of(yearNum, monthNum, 1);
			LocalDate lastDayOfMonth =
					firstLocalDate.withDayOfMonth(firstLocalDate.lengthOfMonth());
			Date date = Date.valueOf(firstLocalDate);
			Date lastDate = Date.valueOf(lastDayOfMonth);
			int count = countBillingStatusRecord(date);

			if (count > 0) {
				logger.info("指定された年月の請求情報は既に存在します。");
				return;
			}
			logger.info(year + "年" + month + "月分の請求情報を確認しています。");

			deleteBillingData(date);
			logger.info("データベースから" + year + "年" + month + "月分の未確定請求情報を削除しました.");

			logger.info(year + "年" + month + "月分の請求ステータス情報を追加しています。");

			int statusCount = insertBillingStatus(date);
			logger.info(statusCount + "件追加しました。");

			logger.info(year + "年" + month + "月分の請求データ情報を追加しています。");
			countInsertBillingDataAndDetailData countInsertlData =
					insertBillingDataAndDetailData(date, lastDate);

			if (countInsertlData.memberCount == 0) {
				logger.error("有効な加入者情報が存在しませんでした。");
				logger.info("-".repeat(40));
				return;
			}
			//請求データの挿入レコード数
			logger.info(countInsertlData.memberCount + "件追加しました。");

			logger.info(year + "年" + month + "月分の請求明細データ情報を追加しています。");

			//請求明細データの挿入レコード数
			logger.info(countInsertlData.chargeCount + "件追加しました。");

		} catch (DataAccessException e) {
			logger.error("データベースアクセス中にエラーが発生しました。");
		} catch (NullPointerException e) {
			logger.error("有効な料金情報が存在しませんでした。");
		} catch (DateTimeException e) {
			logger.error("正しい年月を入力してください。");
		}
		logger.info("-".repeat(40));
	}
}
