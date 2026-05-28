package com.example.sppt.dto;

/**
 * @author sjy
 * @since 2026-05-28
 */
import lombok.Data;

import java.util.List;

@Data
public class ApplyProcessConfigDTO {
    // 审批级数
    private Integer nodeLevel;
    // 各层级审核人ID列表
    private List<List<String>> auditUserIdsList;
    // 区域ID
    private Integer areaId;
}
