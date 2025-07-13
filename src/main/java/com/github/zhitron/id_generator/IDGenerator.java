package com.github.zhitron.id_generator;

/**
 * ID生成器接口，用于定义不同类型的ID生成方法。
 *
 * @author zhitron
 */
public interface IDGenerator {

    /**
     * 生成下一个整型ID。
     *
     * @return 下一个整型ID
     * @throws UnsupportedOperationException 如果不支持生成整型ID，则抛出此异常
     */
    default int generateNextIntID() throws UnsupportedOperationException{
        throw new UnsupportedOperationException("The next int ID could not be generated");
    }

    /**
     * 生成下一个长整型ID，默认实现为将整型ID转换为长整型。
     *
     * @return 下一个长整型ID
     * @throws UnsupportedOperationException 如果不支持生成长整型ID，则抛出此异常
     */
    default long generateNextLongID() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The next long ID could not be generated");
    }

    /**
     * 生成下一个字符串类型ID，默认实现为将整型ID转换为字符串。
     *
     * @return 下一个字符串类型ID
     * @throws UnsupportedOperationException 如果不支持生成字符串类型ID，则抛出此异常
     */
    default String generateNextStringID() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The next String ID could not be generated");
    }
}
