package com.s_giken.training.webapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.s_giken.training.webapp.model.entity.Charge;
import com.s_giken.training.webapp.service.ChargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

		List<Charge> chargeList = chargeService.findChargesForMonth(year, month);
		List<Charge> nextChargeList = chargeService.findChargesForNextMonth(year, month);

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
