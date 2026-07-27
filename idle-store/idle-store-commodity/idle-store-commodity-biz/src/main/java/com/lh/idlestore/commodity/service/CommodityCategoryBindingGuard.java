package com.lh.idlestore.commodity.service;

/**
 * 商品或子分类绑定分类前的并发安全校验。
 *
 * <p>调用方必须已经开启数据库事务，并在本方法返回后、事务提交前完成
 * 商品或子分类的写入；否则共享锁无法保护“分类校验 + 绑定写入”这个整体。</p>
 */
public interface CommodityCategoryBindingGuard {

    /**
     * 对目标分类到根分类的路径加共享锁，并校验整条路径均可用。
     *
     * @param categoryId 要绑定的分类 ID
     */
    void checkCategoryCanBind(Long categoryId);

    /**
     * 对目标分类到根分类的路径加共享锁，校验整条路径可用且目标分类版本未变化。
     * 商品发布或修改商品分类时使用。
     *
     * @param categoryId 要绑定的分类 ID
     * @param expectedVersion 用户选择分类时看到的版本号
     */
    void checkCategoryCanBind(Long categoryId, Integer expectedVersion);
}
