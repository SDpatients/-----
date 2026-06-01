package com.supplier.test.scripts;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * 前端 API 路径对比校验脚本
 * <p>
 * 目的：自动检测前端 API 模块中的路径定义是否与后端 Controller 定义一致。
 * 解决 5月23日 BUG 汇总中发现的 BUG-6 ~ BUG-12 等路径不一致问题。
 * <p>
 * 使用方式：运行 main 方法即可输出校验报告。
 * 后续可集成到 CI 中作为构建门禁。
 */
public class ApiPathValidator {

    // 后端 Controller 所在目录
    private static final String BACKEND_CONTROLLER_DIR = "backend/src/main/java/com/supplier";

    // 前端 API 模块所在目录
    private static final String FRONTEND_API_DIR = "frontend/src/api";

    // 项目根目录
    private static final String ROOT_DIR = "d:/.我的项目/供应商协同";

    /**
     * 已知的前后端路径映射表（手动维护，作为期望值基准）
     * key: 前端文件中的路径前缀
     * value: 后端 Controller 中的正确路径
     */
    private static final Map<String, String> KNOWN_PATH_MAPPINGS = Map.ofEntries(
            // 供应商
            Map.entry("supplier.ts", "/v1/suppliers"),
            // 订单
            Map.entry("order.ts", "/v1/purchase-orders"),
            Map.entry("orderDetail.ts", "/v1/purchase-order-details"),
            Map.entry("orderChange.ts", "/v1/order-changes"),
            // 物流/ASN
            Map.entry("logistics.ts", "/v1/delivery-notices"),
            // 质量检验
            Map.entry("quality.ts", "/v1/quality-inspections"),
            // 对账
            Map.entry("settlement.ts", "/v1/reconciliations"),
            // 财务
            Map.entry("finance.ts", "/v1/invoices, /v1/supplier-performances, /v1/payments, /v1/deductions"),
            // 质量管理
            Map.entry("qualityExtra.ts", "/v1/nonconformance-reports, /v1/eight-d-reports, /v1/quality-appeals"),
            // 寻源
            Map.entry("sourcing.ts", "/v1/rfqs, /v1/quotes"),
            // 库存
            Map.entry("inventory.ts", "/v1/vmi-inventories"),
            // 集成
            Map.entry("integration.ts", "/v1/integration-endpoints"),
            // 附件
            Map.entry("attachment.ts", "/v1/file-attachments"),
            // 操作日志
            Map.entry("operationLog.ts", "/v1/audit-logs"),
            // 消息通知
            Map.entry("notification.ts", "/v1/message-notices"),
            // 配置
            Map.entry("config.ts", "/v1/sys-configs"),
            // 导入导出
            Map.entry("importExport.ts", "/v1/import-tasks, /v1/export-tasks"),
            // 搜索
            Map.entry("search.ts", "/v1/search"),
            // 仪表盘
            Map.entry("dashboard.ts", "/v1/dashboard"),
            // 认证
            Map.entry("auth.ts", "/auth"),
            // 公共
            Map.entry("common.ts", "/v1/common"),
            // 黑名单
            Map.entry("blacklist.ts", "/v1/supplier-blacklists"),
            // 适配器
            Map.entry("adapters.ts", ""),
            // Mock
            Map.entry("mockApi.ts", ""),
            Map.entry("mockData.ts", ""),
            Map.entry("mockInterceptor.ts", "")
    );

    public static void main(String[] args) throws IOException {
        System.out.println("=".repeat(60));
        System.out.println("   前后端 API 路径一致性校验报告");
        System.out.println("=".repeat(60));
        System.out.println();

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int verifiedCount = 0;

        // 扫描后端 Controller 提取 @RequestMapping
        Map<String, List<String>> backendPaths = scanBackendControllers();
        System.out.println("【后端 Controller 扫描结果】共 " + backendPaths.size() + " 个 Controller");

        // 扫描前端 API 模块提取路径
        Map<String, List<String>> frontendPaths = scanFrontendApiModules();
        System.out.println("【前端 API 模块扫描结果】共 " + frontendPaths.size() + " 个模块");
        System.out.println();

        // 对比检查
        for (Map.Entry<String, List<String>> entry : frontendPaths.entrySet()) {
            String fileName = entry.getKey();
            List<String> apiPaths = entry.getValue();

            if (apiPaths.isEmpty()) continue;

            String expectedPaths = KNOWN_PATH_MAPPINGS.getOrDefault(fileName, "");
            if (expectedPaths.isEmpty()) {
                warnings.add("⚠ " + fileName + ": 缺少已知路径映射配置，请手动验证");
                continue;
            }

            for (String apiPath : apiPaths) {
                boolean match = false;
                for (String expectedPath : expectedPaths.split(",\\s*")) {
                    if (apiPath.startsWith(expectedPath)) {
                        match = true;
                        verifiedCount++;
                        break;
                    }
                }
                if (!match) {
                    // 检查后端是否有匹配的路径
                    boolean backendMatch = false;
                    for (Map.Entry<String, List<String>> be : backendPaths.entrySet()) {
                        for (String bePath : be.getValue()) {
                            if (apiPath.startsWith(bePath)) {
                                backendMatch = true;
                                break;
                            }
                        }
                        if (backendMatch) break;
                    }

                    if (!backendMatch) {
                        issues.add("✗ " + fileName + ": 前端路径 [" + apiPath
                                + "] 在后端 Controller 中找不到匹配项！"
                                + " 期望: " + expectedPaths);
                    }
                }
            }
        }

        // 输出结果
        System.out.println("-".repeat(60));
        System.out.println("【校验结果】");
        System.out.println("-".repeat(60));
        System.out.println("✓ 验证通过: " + verifiedCount + " 条路径");
        System.out.println("⚠ 警告: " + warnings.size() + " 条");
        System.out.println("✗ 错误: " + issues.size() + " 条");
        System.out.println();

        if (!warnings.isEmpty()) {
            System.out.println("## 警告:");
            warnings.forEach(System.out::println);
            System.out.println();
        }

        if (!issues.isEmpty()) {
            System.out.println("## 路径不一致错误 (需紧急修复):");
            issues.forEach(System.out::println);
            System.out.println();
            System.out.println("!!! 上述路径不一致将导致对应功能 404，请立即修复 !!!");
            System.exit(1);
        } else {
            System.out.println("✓ 所有已配置的 API 路径校验通过！");
            System.exit(0);
        }
    }

    /**
     * 扫描后端 Controller 文件，提取 @RequestMapping 注解中的路径
     */
    private static Map<String, List<String>> scanBackendControllers() throws IOException {
        Map<String, List<String>> paths = new TreeMap<>();
        Path root = Paths.get(ROOT_DIR, BACKEND_CONTROLLER_DIR);

        if (!Files.exists(root)) {
            System.out.println("警告: 后端目录不存在 - " + root);
            return paths;
        }

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith("Controller.java")) {
                    String content = Files.readString(file);
                    List<String> requestMappings = extractRequestMapping(content);
                    if (!requestMappings.isEmpty()) {
                        paths.put(fileName.replace(".java", ""), requestMappings);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return paths;
    }

    /**
     * 扫描前端 API 模块，提取请求路径
     */
    private static Map<String, List<String>> scanFrontendApiModules() throws IOException {
        Map<String, List<String>> paths = new TreeMap<>();
        Path root = Paths.get(ROOT_DIR, FRONTEND_API_DIR);

        if (!Files.exists(root)) {
            System.out.println("警告: 前端目录不存在 - " + root);
            return paths;
        }

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".ts") || fileName.endsWith(".js")) {
                    String content = Files.readString(file);
                    List<String> apiPaths = extractApiPaths(content);
                    if (!apiPaths.isEmpty()) {
                        paths.put(fileName, apiPaths);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return paths;
    }

    /**
     * 从 Java 代码中提取 @RequestMapping 路径
     */
    private static List<String> extractRequestMapping(String content) {
        List<String> paths = new ArrayList<>();
        // 匹配 @RequestMapping("/v1/xxx") 或 @RequestMapping({"/v1/xxx", "/v1/yyy"})
        Pattern pattern = Pattern.compile("@RequestMapping\\s*\\(\\s*(?:value\\s*=\\s*)?\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            paths.add(matcher.group(1));
        }
        return paths;
    }

    /**
     * 从 TypeScript 代码中提取 API 请求路径
     */
    private static List<String> extractApiPaths(String content) {
        List<String> paths = new ArrayList<>();
        // 匹配 /v1/xxx 或 /auth/xxx 路径
        Pattern pattern = Pattern.compile("['\"`]/(v1|auth|common|api)/[^'\"`]+['\"`]");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String path = matcher.group();
            // 去掉引号
            path = path.substring(1, path.length() - 1);
            // 过滤模板字符串中的变量占位符 ${}
            path = path.replaceAll("\\$\\{[^}]+\\}", "{id}");
            if (!paths.contains(path)) {
                paths.add(path);
            }
        }
        return paths;
    }
}