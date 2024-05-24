package com.s_giken.training.webapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.s_giken.training.webapp.model.entity.Charge;
import com.s_giken.training.webapp.service.ChargeService;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * ルートパスのコントローラークラス
 */
@Controller
@RequestMapping("/")
public class RootController {
	@Autowired
	private ChargeService chargeService;

	/**
	 * ルートパスにアクセスされた場合の処理
	 * 
	 * @return トップ画面のテンプレート名
	 */
	@GetMapping("/")
	public String hello(Model model) {
		LocalDateTime today = LocalDateTime.now();
		String formatDate = today.format(DateTimeFormatter.ofPattern("yyyyMM"));
		String year = formatDate.substring(0, 4);
		String month = formatDate.substring(4);

		var chargeSearchCondition = chargeService.findAll();
		List<Charge> chargeList = new ArrayList<>();
		List<Charge> nextChargeList = new ArrayList<>();

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

		int count = chargeList.size();
		int nextCount = nextChargeList.size();
		model.addAttribute("count", count);
		model.addAttribute("chargeList", chargeList);
		model.addAttribute("nextCount", nextCount);
		model.addAttribute("nextChargeList", nextChargeList);
		return "top";
	}

	/**
	 * ログイン画面を表示する
	 * 
	 * @return ログイン画面のテンプレート名
	 */
	@GetMapping("/login")
	public String login() {
		return "login";
	}
}
