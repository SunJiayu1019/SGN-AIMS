package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sppt.entity.ApplyForm;
import com.example.sppt.entity.HouseInfo;
import com.example.sppt.entity.SysArea;
import com.example.sppt.service.ApplyFormService;
import com.example.sppt.service.HousePdfService;
import com.example.sppt.service.HouseService;
import com.example.sppt.service.SysAreaService;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 门牌信息 PDF 导出实现。
 * 中文渲染：iText5 + 亚洲字体（STSong-Light + UniGB-UCS2-H），无需自带 ttf。
 * @author sjy
 */
@Service
@RequiredArgsConstructor
public class HousePdfServiceImpl implements HousePdfService {

    private final HouseService houseService;
    private final SysAreaService sysAreaService;
    private final ApplyFormService applyFormService;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public byte[] exportHousePdf(Long houseId) {
        HouseInfo house = houseService.getById(houseId);
        if (house == null) {
            throw new IllegalArgumentException("门牌不存在");
        }

        try {
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font fontTitle = new Font(bf, 18, Font.BOLD);
            Font fontH2 = new Font(bf, 13, Font.BOLD);
            Font fontLabel = new Font(bf, 11, Font.BOLD);
            Font fontText = new Font(bf, 11);
            Font fontSmall = new Font(bf, 9, Font.NORMAL, BaseColor.GRAY);

            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // 标题
            Paragraph title = new Paragraph("门牌信息明细表", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            doc.add(title);

            Paragraph subtitle = new Paragraph("门牌编号：" + safe(house.getHouseCode()), fontSmall);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(16);
            doc.add(subtitle);

            // 一、基本信息
            doc.add(section("一、门牌基本信息", fontH2));
            PdfPTable info = new PdfPTable(new float[]{1, 2.2f});
            info.setWidthPercentage(100);
            info.setSpacingBefore(6);
            info.setSpacingAfter(16);
            kv(info, "门牌编号", safe(house.getHouseCode()), fontLabel, fontText);
            kv(info, "详细地址", safe(house.getAddress()), fontLabel, fontText);
            kv(info, "房屋类型", houseTypeText(house.getHouseType()), fontLabel, fontText);
            kv(info, "所属区域", areaName(house.getAreaId()), fontLabel, fontText);
            kv(info, "经度", safe(house.getLng()), fontLabel, fontText);
            kv(info, "纬度", safe(house.getLat()), fontLabel, fontText);
            kv(info, "地理坐标", safe(house.getGeometry()), fontLabel, fontText);
            kv(info, "状态", statusText(house.getStatus()), fontLabel, fontText);
            doc.add(info);

            // 二、相关申请记录（通过 house_info.apply_no 关联 apply_form.apply_no）
            doc.add(section("二、相关门牌申请记录", fontH2));
            List<ApplyForm> applies = (house.getApplyNo() == null || house.getApplyNo().isEmpty())
                    ? java.util.Collections.emptyList()
                    : applyFormService.list(new LambdaQueryWrapper<ApplyForm>()
                            .eq(ApplyForm::getApplyNo, house.getApplyNo())
                            .orderByDesc(ApplyForm::getCreateTime));

            if (applies == null || applies.isEmpty()) {
                Paragraph none = new Paragraph("（暂无与该门牌关联的申请记录）", fontText);
                none.setSpacingBefore(6);
                doc.add(none);
            } else {
                PdfPTable tb = new PdfPTable(new float[]{1.6f, 1f, 1f, 1.3f, 1f, 1.4f});
                tb.setWidthPercentage(100);
                tb.setSpacingBefore(6);
                String[] heads = {"申请单号", "类型", "申请人", "联系电话", "状态", "申请时间"};
                for (String h : heads) {
                    PdfPCell c = new PdfPCell(new Phrase(h, fontLabel));
                    c.setBackgroundColor(new BaseColor(244, 246, 248));
                    c.setPadding(5);
                    tb.addCell(c);
                }
                for (ApplyForm a : applies) {
                    tb.addCell(cell(safe(a.getApplyNo()), fontText));
                    tb.addCell(cell(applyTypeText(a.getApplyType()), fontText));
                    tb.addCell(cell(safe(a.getApplicantName()), fontText));
                    tb.addCell(cell(safe(a.getContactPhone()), fontText));
                    tb.addCell(cell(applyStatusText(a.getStatus()), fontText));
                    tb.addCell(cell(a.getCreateTime() == null ? "-" : a.getCreateTime().format(DTF), fontText));
                }
                doc.add(tb);
            }

            // 页脚
            Paragraph foot = new Paragraph(
                    "导出时间：" + java.time.LocalDateTime.now().format(DTF)
                            + "　　地名地址审批系统", fontSmall);
            foot.setAlignment(Element.ALIGN_RIGHT);
            foot.setSpacingBefore(24);
            doc.add(foot);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成PDF失败：" + e.getMessage(), e);
        }
    }

    // ---------- 小工具 ----------
    private Paragraph section(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(8);
        return p;
    }

    private void kv(PdfPTable t, String k, String v, Font fk, Font fv) {
        PdfPCell ck = new PdfPCell(new Phrase(k, fk));
        ck.setBackgroundColor(new BaseColor(248, 249, 250));
        ck.setPadding(6);
        PdfPCell cv = new PdfPCell(new Phrase(v, fv));
        cv.setPadding(6);
        t.addCell(ck);
        t.addCell(cv);
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(5);
        return c;
    }

    private String safe(String s) { return (s == null || s.isEmpty()) ? "-" : s; }

    private String areaName(Long areaId) {
        if (areaId == null || areaId == 0) return "总站 / 未指定";
        SysArea a = sysAreaService.getById(areaId);
        return a == null ? ("未知(" + areaId + ")") : a.getName();
    }

    private String houseTypeText(String t) {
        if ("house".equals(t)) return "住宅";
        if ("shop".equals(t)) return "商铺";
        if ("factory".equals(t)) return "厂房";
        return t == null ? "-" : t;
    }

    private String statusText(Integer s) {
        if (s == null) return "-";
        return s == 1 ? "启用" : "停用";
    }

    private String applyTypeText(String t) {
        if ("new".equals(t)) return "新门牌申请";
        if ("reissue".equals(t)) return "门牌补发";
        return t == null ? "-" : t;
    }

    private String applyStatusText(String s) {
        if ("PENDING".equals(s)) return "待审批";
        if ("APPROVED".equals(s)) return "已通过";
        if ("REJECTED".equals(s)) return "已驳回";
        return s == null ? "-" : s;
    }
}
