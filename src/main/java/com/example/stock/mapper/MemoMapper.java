package com.example.stock.mapper;

import com.example.stock.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MemoMapper {

    // ---- 分类 ----
    List<MemoCategory> listCategories();
    MemoCategory getCategory(@Param("id") Integer id);
    int insertCategory(MemoCategory category);
    int updateCategory(MemoCategory category);
    int deleteCategory(@Param("id") Integer id);

    // ---- 标签 ----
    List<MemoTag> listTags();
    MemoTag getTag(@Param("id") Integer id);
    int insertTag(MemoTag tag);
    int updateTag(MemoTag tag);
    int deleteTag(@Param("id") Integer id);

    // ---- 作者 ----
    List<MemoAuthor> listAuthors();
    MemoAuthor getAuthor(@Param("id") Integer id);
    int insertAuthor(MemoAuthor author);
    int deleteAuthor(@Param("id") Integer id);

    // ---- 备忘录 ----
    List<Memo> listMemos(@Param("categoryId") Integer categoryId,
                         @Param("authorId") Integer authorId,
                         @Param("tagId") Integer tagId,
                         @Param("keyword") String keyword,
                         @Param("star") Integer star);
    Memo getMemo(@Param("id") Integer id);
    int insertMemo(Memo memo);
    int updateMemo(Memo memo);
    int deleteMemo(@Param("id") Integer id);
    int updateMemoTime(@Param("id") Integer id);

    // ---- 标签映射 ----
    List<MemoTag> listTagsByMemo(@Param("memoId") Integer memoId);
    int insertTagMapping(MemoTagMapping mapping);
    int deleteTagMapping(@Param("memoId") Integer memoId);
}
