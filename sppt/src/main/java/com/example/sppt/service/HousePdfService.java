package com.example.sppt.service;

/**
 * 门牌信息 PDF 导出 Service（跨表：house_info + sys_area + apply_form），
 * 故为普通 @Service，只依赖其它实体 Service。
 * @author sjy
 */
public interface HousePdfService {

    // 生成"某个门牌的全部相关信息"PDF，返回字节数组
    byte[] exportHousePdf(Long houseId);
}
