package cc.ddrpa.dorian.polystash.core.blobstore;

/**
 * 列表选项记录类，用于配置 Blob 对象列举的行为。
 * <p>
 * 该类定义了在列举存储对象时可以使用的各种选项，包括路径分隔符、
 * 递归模式等。通过配置这些选项，可以控制列举结果的格式和范围。
 * <p>
 * 使用记录类（record）确保不可变性，并提供便捷的构造和访问方法。
 *
 * @param delimiter 存储系统使用的路径分隔符
 * @param recursive 是否递归查找子目录中的文件
 */
public record ListOptions(
        /**
         * 存储系统使用的路径分隔符。
         * <p>
         * 用于在列举结果中区分目录和文件。常见的分隔符包括 "/"（Unix/Linux）
         * 和 "\\"（Windows）。分隔符的选择应该与存储系统的路径约定保持一致。
         */
        String delimiter,
        /**
         * 是否递归查找子目录中的文件。
         * <p>
         * 当设置为 true 时，列举操作会遍历指定前缀下的所有子目录，
         * 返回完整的文件树结构。当设置为 false 时，只返回指定前缀
         * 下的直接文件和目录，不进行递归遍历。
         */
        boolean recursive
) {
    /**
     * 默认的列表选项实例。
     * <p>
     * 使用 "/" 作为路径分隔符，启用递归模式，适用于大多数 Unix/Linux
     * 风格的存储系统。
     */
    private static final ListOptions DEFAULT = new ListOptions("/", true);

    /**
     * 获取默认的列表选项实例。
     * <p>
     * 返回预配置的默认选项，适用于大多数常见场景。
     * 默认配置使用 "/" 分隔符并启用递归模式。
     *
     * @return 默认的列表选项实例
     */
    public static ListOptions withDefault() {
        return DEFAULT;
    }
}