package cc.ddrpa.dorian.polystash.utils;

/**
 * 字符串常量池，提供常用的字符串常量定义。
 * <p>
 * 该类定义了在 PolyStash 项目中经常使用的字符串常量，包括：
 * <ul>
 *   <li>常用的分隔符和标点符号</li>
 *   <li>HTTP 内容类型常量</li>
 *   <li>空字符串和空格等基础字符串</li>
 * </ul>
 * <p>
 * 使用常量池的好处：
 * <ul>
 *   <li>避免字符串字面量重复，节省内存</li>
 *   <li>统一管理常用字符串，便于维护</li>
 *   <li>减少拼写错误的可能性</li>
 *   <li>提高代码的可读性和一致性</li>
 * </ul>
 * <p>
 * 该类被设计为工具类，不能实例化，所有常量都是静态的。
 */
public class StringPool {

    /**
     * 空字符串常量。
     * <p>
     * 用于表示空字符串，避免使用 null 或 "" 字面量。
     */
    public static final String EMPTY = "";

    /**
     * 空格字符串常量。
     * <p>
     * 用于表示单个空格字符，常用于字符串拼接和格式化。
     * </p>
     */
    public static final String SPACE = " ";

    /**
     * 逗号分隔符常量。
     * <p>
     * 用于 CSV 格式、列表分隔等场景。
     * </p>
     */
    public static final String COMMA = ",";

    /**
     * 冒号分隔符常量。
     * <p>
     * 用于键值对分隔、时间格式等场景。
     */
    public static final String COLON = ":";

    /**
     * 分号分隔符常量。
     * <p>
     * 用于多值分隔、配置项分隔等场景。
     */
    public static final String SEMICOLON = ";";

    /**
     * 句点分隔符常量。
     * <p>
     * 用于文件扩展名、域名分隔等场景。
     */
    public static final String PERIOD = ".";

    /**
     * 斜杠分隔符常量。
     * <p>
     * 用于路径分隔、URL 分隔等场景。
     */
    public static final String SLASH = "/";

    /**
     * 二进制流内容类型常量。
     * <p>
     * 表示通用的二进制数据流，常用于文件下载和二进制数据传输。
     * 这是 HTTP Content-Type 头部的标准值。
     */
    public static final String CONTENT_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类被设计为工具类，只包含静态常量，不应该被实例化。
     * 如果尝试实例化，将抛出 IllegalStateException。
     *
     * @throws IllegalStateException 当尝试实例化此类时抛出
     */
    private StringPool() {
        throw new IllegalStateException("Utility class");
    }
}
