package com.example.stock.service;

import com.example.stock.dto.ApiResult;
import com.example.stock.dto.MemoDetailDTO;
import com.example.stock.entity.*;
import com.example.stock.mapper.MemoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoService {

    @Autowired
    private MemoMapper memoMapper;

    // ========== 分类 ==========
    public List<MemoCategory> listCategories() {
        return memoMapper.listCategories();
    }

    public ApiResult<?> saveCategory(MemoCategory c) {
        if (c.getId() == null) {
            memoMapper.insertCategory(c);
        } else {
            memoMapper.updateCategory(c);
        }
        return ApiResult.success(c);
    }

    public ApiResult<?> deleteCategory(Integer id) {
        memoMapper.deleteCategory(id);
        return ApiResult.success(null);
    }

    // ========== 标签 ==========
    public List<MemoTag> listTags() {
        return memoMapper.listTags();
    }

    public ApiResult<?> saveTag(MemoTag t) {
        if (t.getId() == null) {
            memoMapper.insertTag(t);
        } else {
            memoMapper.updateTag(t);
        }
        return ApiResult.success(t);
    }

    public ApiResult<?> deleteTag(Integer id) {
        memoMapper.deleteTag(id);
        return ApiResult.success(null);
    }

    // ========== 作者 ==========
    public List<MemoAuthor> listAuthors() {
        return memoMapper.listAuthors();
    }

    public ApiResult<?> addAuthor(MemoAuthor a) {
        memoMapper.insertAuthor(a);
        return ApiResult.success(a);
    }

    public ApiResult<?> deleteAuthor(Integer id) {
        memoMapper.deleteAuthor(id);
        return ApiResult.success(null);
    }

    // ========== 备忘录 ==========
    private MemoDetailDTO toDetail(Memo m) {
        MemoDetailDTO dto = new MemoDetailDTO();
        dto.setId(m.getId());
        dto.setTitle(m.getTitle());
        dto.setContent(m.getContent());
        dto.setStars(m.getStars());
        dto.setPin(m.getPin());
        dto.setCreateTime(m.getCreateTime());
        dto.setUpdateTime(m.getUpdateTime());

        if (m.getAuthorId() != null) {
            MemoAuthor a = memoMapper.getAuthor(m.getAuthorId());
            if (a != null) {
                dto.setAuthorId(a.getId());
                dto.setAuthorName(a.getName());
                dto.setAuthorColor(a.getAvatarColor());
            }
        }
        if (m.getCategoryId() != null) {
            MemoCategory c = memoMapper.getCategory(m.getCategoryId());
            if (c != null) {
                dto.setCategoryId(c.getId());
                dto.setCategoryName(c.getName());
                dto.setCategoryColor(c.getColor());
            }
        }
        List<MemoTag> tags = memoMapper.listTagsByMemo(m.getId());
        dto.setTags(tags.stream().map(t -> {
            MemoDetailDTO.TagInfo ti = new MemoDetailDTO.TagInfo();
            ti.setId(t.getId());
            ti.setName(t.getName());
            ti.setColor(t.getColor());
            return ti;
        }).collect(Collectors.toList()));
        return dto;
    }

    public List<MemoDetailDTO> listMemos(Integer categoryId, Integer authorId, Integer tagId, String keyword, Integer star) {
        List<Memo> list = memoMapper.listMemos(categoryId, authorId, tagId, keyword, star);
        return list.stream().map(this::toDetail).collect(Collectors.toList());
    }

    public MemoDetailDTO getMemo(Integer id) {
        Memo m = memoMapper.getMemo(id);
        return m == null ? null : toDetail(m);
    }

    @Transactional
    public ApiResult<?> saveMemo(Memo memo, List<Integer> tagIds) {
        boolean isNew = memo.getId() == null;
        if (isNew) {
            memo.setPin(false);
            memoMapper.insertMemo(memo);
        } else {
            memoMapper.updateMemo(memo);
            memoMapper.deleteTagMapping(memo.getId());
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Integer tid : tagIds) {
                MemoTagMapping mapping = new MemoTagMapping();
                mapping.setMemoId(memo.getId());
                mapping.setTagId(tid);
                memoMapper.insertTagMapping(mapping);
            }
        }
        return ApiResult.success(getMemo(memo.getId()));
    }

    @Transactional
    public ApiResult<?> deleteMemo(Integer id) {
        memoMapper.deleteTagMapping(id);
        memoMapper.deleteMemo(id);
        return ApiResult.success(null);
    }
}
