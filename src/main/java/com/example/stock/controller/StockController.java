package com.example.stock.controller;

import com.example.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/stock")
    public String stockPage() {
        return "forward:/stock.html";
    }

    @GetMapping("/api/stock/hot")
    @ResponseBody
    public Map<String, Object> hotData() {
        return stockService.fetchHotData();
    }
}
