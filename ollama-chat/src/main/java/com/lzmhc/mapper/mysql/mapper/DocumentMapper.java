package com.lzmhc.mapper.mysql.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.lzmhc.mapper.mysql.entity.Document;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
@DS(value = "mysql")
public interface DocumentMapper {
    @Insert("INSERT INTO t_file_knowledge(folder_type, file_name, file_size, file_key, file_type, creator_id, is_embedding) values(#{folderType}, #{fileName}, #{fileSize}, #{fileKey}, #{fileType}, #{creatorId}, #{isEmbedding})")
    int insertDocument(Document document);
    @Select("select file_id, file_name, file_size, file_key, file_type, create_time, is_embedding from t_file_knowledge where creator_id = #{userId}")
    @Results({
            @Result(property = "fileId", column = "file_id"),
            @Result(property = "fileName", column = "file_name"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "fileKey", column = "file_key"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "isEmbedding", column = "is_embedding")
    })
    List<Document> getDocumentList( @Param("userId") String userId );
    @Update("update t_file_knowledge set is_embedding = 1 where file_name = #{fileName}")
    int updateEmbedding(@Param("fileName") String fileName);
    @Delete("delete from t_file_knowledge where file_id = #{id}")
    int deleteDocument(@Param("id") String id);

    @Select("select file_id from t_file_knowledge where file_name = #{fileName}")
    int getDocumentId(@Param("fileName") String fileName);

    @Select("select file_id, file_name from t_file_knowledge where creator_id = #{userId} and is_embedding = 1")
    @Results({
            @Result(property = "fileId", column = "file_id"),
            @Result(property = "fileName", column = "file_name")
    })
    List<Document> getKnowledgeList( @Param("userId") String userId );

    @Select("select file_id, file_name, file_size, file_key, file_type, creator_name, create_time from t_file ")
    List<Document> getFileList();

    @Insert("INSERT INTO t_file_multimodal(folder_type, file_name, file_size, file_key, file_type, creator_id, is_embedding) values(#{folderType}, #{fileName}, #{fileSize}, #{fileKey}, #{fileType}, #{creatorId}, #{isEmbedding})")
    int insertMultimodal(Document document);

    @Insert("INSERT INTO t_file_homework(folder_type, file_name, file_size, file_key, file_type, creator_id, is_embedding) values(#{folderType}, #{fileName}, #{fileSize}, #{fileKey}, #{fileType}, #{creatorId}, #{isEmbedding})")
    int insertHomework(Document document);
}
