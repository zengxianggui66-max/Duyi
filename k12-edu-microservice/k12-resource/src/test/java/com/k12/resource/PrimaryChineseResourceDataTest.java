// package com.k12.resource;

// import com.baomidou.mybatisplus.core.metadata.IPage;
// import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// import com.k12.common.entity.PrimaryChineseResource;
// import com.k12.resource.mapper.PrimaryChineseResourceMapper;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;

// /**
//  * oss_primary_chinese_resource 完整测试类（插入 + 查询验证）
//  *
//  * 测试内容：
//  * 1. insertTestData()    — 插入7条真实OSS数据（一年级下册·第一单元·教案）
//  * 2. testQueryAll()       — 查询全部记录，打印每条的标题和签名URL
//  * 3. testQueryByGrade()   — 按"一年级下册"筛选
//  * 4. testQueryByUnit()    — 按"第一单元"筛选
//  * 5. testQueryByType()    — 按"教案"类型筛选
//  * 6. testQueryKeyword()   — 关键词模糊搜索（如"识字"）
//  * 7. testQueryPage()      — 分页查询（第1页，每页3条）
//  * 8. testFilterOptions()  — 获取所有筛选项枚举（年级/版本/栏目/类型/单元）
//  * 9. testQueryById()      — 按ID查询单条详情
//  *
//  * 执行方式：
//  *   - 全部运行：右键运行整个类
//  *   - 单个方法：右键运行对应方法
//  */
// @SpringBootTest
// public class PrimaryChineseResourceDataTest {

//     @Autowired
//     private PrimaryChineseResourceMapper mapper;

//     // ==================== 插入数据 ====================

//     @Test
//     public void insertTestData() {
//         List<PrimaryChineseResource> list = buildTestData();
//         int total = 0;
//         for (PrimaryChineseResource r : list) {
//             mapper.insert(r);
//             total++;
//         }
//         System.out.println("✅ 插入完成，共插入 " + total + " 条记录");
//     }

//     // ==================== 查询验证 ====================

//     /**
//      * 查询全部记录，逐条输出关键信息
//      * 验证点：确认数据已正确入库，ossUrl 是否包含完整签名参数
//      */
//     @Test
//     public void testQueryAll() {
//         System.out.println("\n========== 查询全部记录 ==========");
//         List<PrimaryChineseResource> all = mapper.findByCondition(null, null, null, null, null, null, null, null, null, null);
//         System.out.println("共 " + all.size() + " 条记录\n");

//         for (int i = 0; i < all.size(); i++) {
//             PrimaryChineseResource r = all.get(i);
//             System.out.println("--- [" + (i + 1) + "] id=" + r.getId() + " ---");
//             System.out.println("  标题:     " + r.getTitle());
//             System.out.println("  年级:     " + r.getGradeName());
//             System.out.println("  版本:     " + r.getEdition());
//             System.out.println("  单元:     " + r.getUnitName());
//             System.out.println("  类型:     " + r.getType());
//             System.out.println("  文件大小: " + r.getFileSizeKb() + " KB");
//             System.out.println("  下载次数: " + r.getDownloadCount());
//             System.out.println("  OSS URL:  " + r.getOssUrl());
//             System.out.println();
//         }

//         assert !all.isEmpty() : "数据库中应该有记录";
//         System.out.println("✅ testQueryAll 通过，共 " + all.size() + " 条");
//     }

//     /**
//      * 按"一年级下册"筛选查询
//      */
//     @Test
//     public void testQueryByGrade() {
//         System.out.println("\n========== 按「一年级下册」筛选 ==========");
//         List<PrimaryChineseResource> list = mapper.findByCondition(
//                 "小学", "语文", null, null, "一年级下册", null, null, null, null, null);

//         for (PrimaryChineseResource r : list) {
//             System.out.println("  id=" + r.getId() + " | " + r.getGradeName()
//                     + " | " + r.getEdition() + " | " + r.getUnitName()
//                     + " | " + r.getTitle());
//         }
//         System.out.println("✅ testQueryByGrade 通过，匹配 " + list.size() + " 条");
//     }

//     /**
//      * 按"第一单元"筛选查询
//      */
//     @Test
//     public void testQueryByUnit() {
//         System.out.println("\n========== 按「第一单元」筛选 ==========");
//         List<PrimaryChineseResource> list = mapper.findByCondition(
//                 "小学", "语文", null, null, null, null, "第一单元", null, null, null);

//         for (PrimaryChineseResource r : list) {
//             System.out.println("  id=" + r.getId() + " | " + r.getUnitName() + " | " + r.getTitle());
//         }
//         System.out.println("✅ testQueryByUnit 通过，匹配 " + list.size() + " 条");
//     }

//     /**
//      * 按"教案"类型筛选
//      */
//     @Test
//     public void testQueryByType() {
//         System.out.println("\n========== 按「教案」类型筛选 ==========");
//         List<PrimaryChineseResource> list = mapper.findByCondition(
//                 null, null, null, "教案", null, null, null, null, null, null);

//         for (PrimaryChineseResource r : list) {
//             System.out.println("  id=" + r.getId() + " | " + r.getType() + " | " + r.getTitle());
//         }
//         System.out.println("✅ testQueryByType 通过，匹配 " + list.size() + " 条");
//     }

//     /**
//      * 关键词模糊搜索（搜"识字"）
//      */
//     @Test
//     public void testQueryKeyword() {
//         System.out.println("\n========== 关键词模糊搜索「识字」 ==========");
//         List<PrimaryChineseResource> list = mapper.findByCondition(
//                 null, null, null, null, null, null, null, null, null, "识字");

//         for (PrimaryChineseResource r : list) {
//             System.out.println("  id=" + r.getId() + " | " + r.getTitle());
//         }
//         System.out.println("✅ testQueryKeyword 通过，命中 " + list.size() + " 条");
//     }

//     /**
//      * 分页查询：第1页，每页3条
//      */
//     @Test
//     public void testQueryPage() {
//         System.out.println("\n========== 分页查询（第1页 / 每页3条） ==========");
//         Page<PrimaryChineseResource> pageParam = new Page<>(1, 3);
//         IPage<PrimaryChineseResource> result = mapper.findByConditionPage(
//                 pageParam,
//                 "小学", "语文", "同步备课", "教案",
//                 "一年级下册", "统编版(2024)", null, null, null, null,
//                 "upload_time", "desc");

//         System.out.println("当前页: " + result.getCurrent());
//         System.out.println("每页数: " + result.getSize());
//         System.out.println("总记录: " + result.getTotal());
//         System.out.println("总页数: " + result.getPages());

//         List<PrimaryChineseResource> records = result.getRecords();
//         for (int i = 0; i < records.size(); i++) {
//             PrimaryChineseResource r = records.get(i);
//             System.out.println("  [" + (i + 1) + "] id=" + r.getId() + " | " + r.getTitle()
//                     + " (" + r.getFileSizeKb() + "KB)");
//         }
//         System.out.println("✅ testQueryPage 通过，本页 " + records.size() + " 条 / 总计 " + result.getTotal() + " 条");
//     }

//     /**
//      * 获取所有筛选项枚举（年级/版本/栏目/类型/单元）—— 对应前端下拉框和侧边树数据源
//      */
//     @Test
//     public void testFilterOptions() {
//         System.out.println("\n========== 筛选项枚举 ==========");

//         // 年级列表
//         List<String> grades = mapper.findDistinctGradeNames();
//         System.out.print("  年级: ");
//         System.out.println(grades);

//         // 版本列表
//         List<String> editions = mapper.findDistinctEditions();
//         System.out.print("  版本: ");
//         System.out.println(editions);

//         // 栏目列表
//         List<String> modules = mapper.findDistinctModules();
//         System.out.print("  栏目: ");
//         System.out.println(modules);

//         // 类型列表
//         List<String> types = mapper.findDistinctTypes();
//         System.out.print("  类型: ");
//         System.out.println(types);

//         // 单元列表（按 一年级下册+统编版(2024)）
//         List<String> units = mapper.findDistinctUnitNames("一年级下册", "统编版(2024)");
//         System.out.print("  单元(一年级下册+统编版): ");
//         System.out.println(units);

//         System.out.println("✅ testFilterOptions 通过");
//     }

//     /**
//      * 按ID查询单条详情（验证 OSS 签名 URL 可访问性信息）
//      * 假设最新插入的记录 ID 为最大值，取第一条即可
//      */
//     @Test
//     public void testQueryById() {
//         System.out.println("\n========== 按ID查询详情 ==========");

//         // 先查全部取第一条
//         List<PrimaryChineseResource> all = mapper.findByCondition(null, null, null, null, null, null, null, null, null, null);
//         if (all.isEmpty()) {
//             System.out.println("⚠️ 数据库无记录，请先执行 insertTestData()");
//             return;
//         }

//         PrimaryChineseResource first = all.get(0);
//         Integer id = first.getId();

//         // 用 MyBatis-Plus 的 selectById
//         PrimaryChineseResource detail = mapper.selectById(id);
//         if (detail == null) {
//             System.out.println("❌ 未找到 id=" + id + " 的记录");
//             return;
//         }

//         System.out.println("  ID:              " + detail.getId());
//         System.out.println("  学段:             " + detail.getStage());
//         System.out.println("  学科:             " + detail.getSubject());
//         System.out.println("  栏目:             " + detail.getModule());
//         System.out.println("  类型:             " + detail.getType());
//         System.out.println("  年级:             " + detail.getGradeName());
//         System.out.println("  版本:             " + detail.getEdition());
//         System.out.println("  单元:             " + detail.getUnitName());
//         System.out.println("  标题:             " + detail.getTitle());
//         System.out.println("  原始文件名:       " + detail.getOriginalFilename());
//         System.out.println("  OSS Bucket:       " + detail.getOssBucket());
//         System.out.println("  OSS ObjectKey:    " + detail.getOssObjectKey());
//         System.out.println("  OSS URL(签名):    " + detail.getOssUrl());
//         System.out.println("  文件大小:         " + detail.getFileSizeKb() + " KB");
//         System.out.println("  下载次数:         " + detail.getDownloadCount());
//         System.out.println("  浏览次数:         " + detail.getViewCount());
//         System.out.println("  状态:             " + detail.getStatus());
//         System.out.println("  上传者ID:         " + detail.getUploaderId());
//         System.out.println("  文件扩展名:       " + detail.getFileExt());
//         System.out.println("  上传时间:         " + detail.getUploadTime());
//         System.out.println("  更新时间:         " + detail.getUpdateTime());
//         System.out.println("  排序权重:         " + detail.getSort());
//         System.out.println("  备注:             " + detail.getRemark());
//         System.out.println("  删除标记:         " + detail.getIsDeleted());

//         // 验证 URL 包含签名必要参数
//         String url = detail.getOssUrl();
//         boolean hasExpires = url.contains("Expires=");
//         boolean hasSignature = url.contains("Signature=");
//         boolean hasAccessKeyId = url.contains("OSSAccessKeyId=");
//         System.out.println("\n  🔍 URL 签名校验:");
//         System.out.println("     Expires 参数:          " + (hasExpires ? "✅ 存在" : "❌ 缺失"));
//         System.out.println("     Signature 参数:        " + (hasSignature ? "✅ 存在" : "❌ 缺失"));
//         System.out.println("     OSSAccessKeyId 参数:  " + (hasAccessKeyId ? "✅ 存在" : "❌ 缺失"));

//         assert hasExpires && hasSignature && hasAccessKeyId : "URL 应包含完整的 OSS 签名参数";
//         System.out.println("✅ testQueryById 通过 — 签名 URL 格式合法，可直接访问文件内容");
//     }

//     /**
//      * 组合条件精确查询（模拟前端实际调用场景）
//      * 场景：小学 → 语文 → 一年级下册 → 统编版(2024) → 第一单元 → 教案
//      */
//     @Test
//     public void testFullConditionQuery() {
//         System.out.println("\n========== 组合条件精确查询（模拟前端场景） ==========");
//         System.out.println("条件: 小学 + 语文 + 一年级下册 + 统编版(2024) + 第一单元 + 教案");

//         List<PrimaryChineseResource> list = mapper.findByCondition(
//                 "小学", "语文", "同步备课", "教案",
//                 "一年级下册", "统编版(2024)", "第一单元", null, null, null);

//         System.out.println("匹配结果: " + list.size() + " 条\n");

//         for (int i = 0; i < list.size(); i++) {
//             PrimaryChineseResource r = list.get(i);
//             System.out.println("  [" + (i + 1) + "] " + r.getTitle());
//             System.out.println("       文件: " + r.getOriginalFilename()
//                     + " | 大小: " + r.getFileSizeKb() + "KB"
//                     + " | 下载: " + r.getDownloadCount() + "次");
//             System.out.println("       URL: " + r.getOssUrl());
//             System.out.println();
//         }

//         assert !list.isEmpty() : "组合条件下应有数据";
//         System.out.println("✅ testFullConditionQuery 通过，匹配 " + list.size() + " 条");
//     }

//     // ===================== 工厂方法 =====================

//     private List<PrimaryChineseResource> buildTestData() {
//         List<PrimaryChineseResource> list = new ArrayList<>();

//         // ================================================================
//         //  真实 OSS 文件数据（一年级下册 · 第一单元 · 教案）
//         //  URL 来自 OSS 控制台签名链接，可直接下载文件内容
//         // ================================================================

//         // 1. 口语交际：听故事，讲故事【教案】.doc — 271KB
//         list.add(buildRealOss(
//                 "一年级下册", "统编版(2024)", "同步备课", "教案", "第一单元",
//                 "口语交际：听故事，讲故事【教案】",
//                 "1.第一单元/口语交际：听故事，讲故事【教案】.doc",
//                 "https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E5%8F%A3%E8%AF%AD%E4%BA%A4%E9%99%85%EF%BC%9A%E5%90%AC%E6%95%85%E4%BA%8B%EF%BC%8C%E8%AE%B2%E6%95%85%E4%BA%8B%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc?Expires=1778516296&OSSAccessKeyId=TMP.3Kzfz6XdnwqTwZzBRMGuH7sv2xERxn3CJhUaUHsCQTEbQDUAv8QhcNd5gWiknqxv8qXptHPSiSZx6gvVMnLf9nG557w7cD&Signature=JwTzaKTHfUlinAoqyNw52%2FxqrTM%3D",
//                 277, 65, LocalDateTime.of(2026, 5, 10, 12, 41)));

//         // 2. 快乐读书吧：读读童谣和儿歌【教案】.doc — 769.5KB
//         list.add(buildRealOss(
//                 "一年级下册", "统编版(2024)", "同步备课", "教案", "第一单元",
//                 "快乐读书吧：读读童谣和儿歌【教案】",
//                 "1.第一单元/快乐读书吧：读读童谣和儿歌【教案】.doc",
//                 "https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E5%BF%AB%E4%B9%90%E8%AF%BB%E4%B9%A6%E5%90%A7%EF%BC%9A%E8%AF%BB%E8%AF%BB%E7%AB%A5%E8%B0%A3%E5%92%8C%E5%84%BF%E6%AD%8C%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc?Expires=1778516296&OSSAccessKeyId=TMP.3Kzfz6XdnwqTwZzBRMGuH7sv2xERxn3CJhUaUHsCQTEbQDUAv8QhcNd5gWiknqxv8qXptHPSiSZx6gvVMnLf9nG557w7cD&Signature=wrX%2BiUZ%2BQ7HTgON%2FL16BK72gpVg%3D",
//                 788, 50, LocalDateTime.of(2026, 5, 10, 12, 41)));

//         // 3. 识字1 春夏秋冬【教案】.doc — 1.278MB
//         list.add(buildRealOss(
//                 "一年级下册", "统编版(2024)", "同步备课", "教案", "第一单元",
//                 "识字1 春夏秋冬【教案】",
//                 "1.第一单元/识字1 春夏秋冬【教案】.doc",
//                 "https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E8%AF%86%E5%AD%971%20%E6%98%A5%E5%A4%8F%E7%A7%8B%E5%86%AC%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc?Expires=1778516296&OSSAccessKeyId=TMP.3Kzfz6XdnwqTwZzBRMGuH7sv2xERxn3CJhUaUHsCQTEbQDUAv8QhcNd5gWiknqxv8qXptHPSiSZx6gvVMnLf9nG557w7cD&Signature=KRj8Okx8if2jwLasltE3r0BKswY%3D",
//                 1309, 88, LocalDateTime.of(2026, 5, 10, 12, 41)));

//         // 4. 识字2 姓氏歌【教案】.doc — 300.5KB
//         list.add(buildRealOss(
//                 "一年级下册", "统编版(2024)", "同步备课", "教案", "第一单元",
//                 "识字2 姓氏歌【教案】",
//                 "1.第一单元/识字2 姓氏歌【教案】.doc",
//                 "https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E8%AF%86%E5%AD%972%20%E5%A7%93%E6%B0%8F%E6%AD%8C%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc?Expires=1778516296&OSSAccessKeyId=TMP.3Kzfz6XdnwqTwZzBRMGuH7sv2xERxn3CJhUaUHsCQTEbQDUAv8QhcNd5gWiknqxv8qXptHPSiSZx6gvVMnLf9nG557w7cD&Signature=xcrBbjS%2FQTWhuCKl1krV7hcJy9o%3D",
//                 308, 72, LocalDateTime.of(2026, 5, 10, 12, 41)));

//         // 5. 识字3 小青蛙【教案】.doc — 875KB
//         list.add(buildRealOss(
//                 "一年级下册", "统编版(2024)", "同步备课", "教案", "第一单元",
//                 "识字3 小青蛙【教案】",
//                 "1.第一单元/识字3 小青蛙【教案】.doc",
//                 "https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E8%AF%86%E5%AD%973%20%E5%B0%8F%E9%9D%92%E8%9B%99%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc?Expires=1778516296&OSSAccessKeyId=TMP.3Kzfz6XdnwqTwZzBRMGuH7sv2xERxn3CJhUaUHsCQTEbQDUAv8QhcNd5gWiknqxv8qXptHPSiSZx6gvVMnLf9nG557w7cD&Signature=m6OK1ccJrzxUlcFVN2QvYi%2Fr194%3D",
//                 896, 60, LocalDateTime.of(2026, 5, 10, 12, 41)));

//         // 6. 识字4 猜字谜【教案】.doc — 2.855MB
//         list.add(buildRealOss(
//                 "一年级下册", "统编版(2024)", "同步备课", "教案", "第一单元",
//                 "识字4 猜字谜【教案】",
//                 "1.第一单元/识字4 猜字谜【教案】.doc",
//                 "https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E8%AF%86%E5%AD%974%20%E7%8C%9C%E5%AD%97%E8%B0%9C%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc?Expires=1778516296&OSSAccessKeyId=TMP.3Kzfz6XdnwqTwZzBRMGuH7sv2xERxn3CJhUaUHsCQTEbQDUAv8QhcNd5gWiknqxv8qXptHPSiSZx6gvVMnLf9nG557w7cD&Signature=w0BNHjDgviEt%2Fk0hz622nrYN32E%3D",
//                 2923, 55, LocalDateTime.of(2026, 5, 10, 12, 43)));

//         // 7. 语文园地【教案】.doc — 1.613MB
//         list.add(buildRealOss(
//                 "一年级下册", "统编版(2024)", "同步备课", "教案", "第一单元",
//                 "语文园地【教案】",
//                 "1.第一单元/语文园地【教案】.doc",
//                 "https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E8%AF%AD%E6%96%87%E5%9B%AD%E5%9C%B0%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc?Expires=1778516296&OSSAccessKeyId=TMP.3Kzfz6XdnwqTwZzBRMGuH7sv2xERxn3CJhUaUHsCQTEbQDUAv8QhcNd5gWiknqxv8qXptHPSiSZx6gvVMnLf9nG557w7cD&Signature=wEnZbxSkQm5citflFm%2BHB%2BoLzgI%3D",
//                 1652, 95, LocalDateTime.of(2026, 5, 10, 12, 41)));

//         return list;
//     }

//     // ===================== 构造辅助方法 =====================

//     private PrimaryChineseResource buildRealOss(
//             String gradeName, String edition, String module, String type, String unitName,
//             String title, String ossObjectKey, String ossUrl,
//             int fileSizeKb, int downloadCount, LocalDateTime uploadTime) {

//         PrimaryChineseResource r = new PrimaryChineseResource();
//         r.setStage("小学");
//         r.setSubject("语文");
//         r.setGradeName(gradeName);
//         r.setEdition(edition);
//         r.setModule(module);
//         r.setType(type);
//         r.setUnitName(unitName);
//         r.setTitle(title);
//         String filename = ossObjectKey.substring(ossObjectKey.lastIndexOf('/') + 1);
//         r.setOriginalFilename(filename);
//         // 从原始文件名提取文件扩展名
//         int dotIdx = filename.lastIndexOf('.');
//         if (dotIdx > 0) {
//             r.setFileExt(filename.substring(dotIdx + 1).toLowerCase());
//         }
//         r.setOssBucket("qier-duuyi");
//         r.setOssObjectKey(ossObjectKey);
//         r.setOssUrl(ossUrl);
//         r.setFileSizeKb(fileSizeKb);
//         r.setDownloadCount(downloadCount);
//         r.setViewCount(0);
//         r.setStatus(1);  // 已发布
//         r.setUploadTime(uploadTime);
//         r.setIsDeleted(0);
//         r.setSort(0);
//         return r;
//     }
// }
