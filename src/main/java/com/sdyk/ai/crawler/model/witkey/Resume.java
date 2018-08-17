package com.sdyk.ai.crawler.model.witkey;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.json.JSON;
import org.elasticsearch.index.engine.DocumentMissingException;

import java.util.Date;
import java.util.List;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "resumes")
public class Resume extends Model {

    @DatabaseField(dataType = DataType.STRING, width = 32)
    public String user_id;

    // 开始时间
    @DatabaseField(dataType = DataType.DATE)
    public Date sd;

    // 结束时间
    @DatabaseField(dataType = DataType.DATE)
    public Date ed;

    // 学校公司
    @DatabaseField(dataType = DataType.STRING, width = 64)
    public String org;

    // 院系
    @DatabaseField(dataType = DataType.STRING, width = 64)
    public String dep;

    // 学位、职位
    @DatabaseField(dataType = DataType.STRING, width = 64)
    public String degree_occupation;

    // 是否在读、在职
    @DatabaseField(dataType = DataType.INTEGER, width = 1)
    public int is_current;

    // 描述
    @DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
    public String content;

    public Resume(){}

    public Resume(String url) {
        super(url);
    }

    /**
     * 插入ES
     * @return
     */
    public boolean insert() {

        super.insert();

        try {

            ServiceProvider serviceProvider = (ServiceProvider) getById(ServiceProvider.class, this.user_id);

            try {
                serviceProvider.updateES();
            } catch (DocumentMissingException e) {
                serviceProvider.insertES();
            }

            return true;
        } catch (Exception e) {

            logger.error("Can not find ServiceProvider: {}", this.user_id, e);
            return false;
        }
    }

}
