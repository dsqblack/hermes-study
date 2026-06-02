package com.example.stock.controller;

import com.example.stock.dto.ApiResult;
import com.example.stock.dto.MemoDetailDTO;
import com.example.stock.entity.*;
import com.example.stock.service.MemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class MemoController {

    @Autowired
    private MemoService memoService;

    @GetMapping("/memo")
    public String memoPage() {
        return "redirect:/memo.html";
    }

    // ========== 分类 ==========
    @GetMapping("/api/memo/categories")
    @ResponseBody
    public List<MemoCategory> listCategories() {
        return memoService.listCategories();
    }

    @PostMapping("/api/memo/categories")
    @ResponseBody
    public ApiResult<?> saveCategory(@RequestBody MemoCategory c) {
        return memoService.saveCategory(c);
    }

    @DeleteMapping("/api/memo/categories/{id}")
    @ResponseBody
    public ApiResult<?> deleteCategory(@PathVariable("id") Integer id) {
        return memoService.deleteCategory(id);
    }

    // ========== 标签 ==========
    @GetMapping("/api/memo/tags")
    @ResponseBody
    public List<MemoTag> listTags() {
        return memoService.listTags();
    }

    @PostMapping("/api/memo/tags")
    @ResponseBody
    public ApiResult<?> saveTag(@RequestBody MemoTag t) {
        return memoService.saveTag(t);
    }

    @DeleteMapping("/api/memo/tags/{id}")
    @ResponseBody
    public ApiResult<?> deleteTag(@PathVariable("id") Integer id) {
        return memoService.deleteTag(id);
    }

    // ========== 作者 ==========
    @GetMapping("/api/memo/authors")
    @ResponseBody
    public List<MemoAuthor> listAuthors() {
        return memoService.listAuthors();
    }

    @PostMapping("/api/memo/authors")
    @ResponseBody
    public ApiResult<?> addAuthor(@RequestBody MemoAuthor a) {
        return memoService.addAuthor(a);
    }

    @DeleteMapping("/api/memo/authors/{id}")
    @ResponseBody
    public ApiResult<?> deleteAuthor(@PathVariable("id") Integer id) {
        return memoService.deleteAuthor(id);
    }

    // ========== 备忘录 ==========
    @GetMapping("/api/memo/memos")
    @ResponseBody
    public List<MemoDetailDTO> listMemos(
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "authorId", required = false) Integer authorId,
            @RequestParam(name = "tagId", required = false) Integer tagId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "star", required = false) Integer star) {
        return memoService.listMemos(categoryId, authorId, tagId, keyword, star);
    }

    @GetMapping("/api/memo/memos/{id}")
    @ResponseBody
    public MemoDetailDTO getMemo(@PathVariable("id") Integer id) {
        return memoService.getMemo(id);
    }

    @PostMapping("/api/memo/memos")
    @ResponseBody
    public ApiResult<?> saveMemo(@RequestBody Map<String, Object> body) {
        Memo memo = new Memo();
        if (body.get("id") != null) {
            memo.setId(Integer.valueOf(body.get("id").toString()));
        }
        memo.setTitle((String) body.get("title"));
        memo.setContent((String) body.get("content"));
        memo.setAuthorId(Integer.valueOf(body.get("authorId").toString()));
        if (body.get("categoryId") != null) {
            memo.setCategoryId(Integer.valueOf(body.get("categoryId").toString()));
        }
        memo.setStars(body.get("stars") != null ? Integer.valueOf(body.get("stars").toString()) : 1);
        memo.setPin(body.get("pin") != null && Boolean.TRUE.equals(body.get("pin")));

        @SuppressWarnings("unchecked")
        List<Integer> tagIds = body.get("tagIds") != null
                ? (List<Integer>) body.get("tagIds")
                : Collections.emptyList();
        return memoService.saveMemo(memo, tagIds);
    }

    @DeleteMapping("/api/memo/memos/{id}")
    @ResponseBody
    public ApiResult<?> deleteMemo(@PathVariable("id") Integer id) {
        return memoService.deleteMemo(id);
    }
}
