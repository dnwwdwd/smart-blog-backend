-- 插入友情链接数据
INSERT INTO friend_link (name, description, avatar, url, is_special, status_label, sort_order, status) VALUES
                                                                                                           ('GitHub', '全球最大的代码托管平台', 'https://github.githubassets.com/favicons/favicon.png', 'https://github.com', false, 'PREMIUM', 10, 1),
                                                                                                           ('掘金', '优质的技术社区', 'https://juejin.cn/favicon.ico', 'https://juejin.cn', true, 'VIP', 5, 1),
                                                                                                           ('V2EX', '创意工作者社区', 'https://www.v2ex.com/static/icon-192.png', 'https://www.v2ex.com', false, NULL, 3, 1),
                                                                                                           ('Stack Overflow', '程序员问答社区', 'https://cdn.sstatic.net/Sites/stackoverflow/Img/favicon.ico', 'https://stackoverflow.com', true, 'PREMIUM', 8, 1),
                                                                                                           ('知乎', '中文互联网高质量问答社区', 'https://static.zhihu.com/heifetz/favicon.ico', 'https://www.zhihu.com', false, NULL, 2, 1);

-- 插入社交图标数据
INSERT INTO social_link (friend_link_id, icon_type, icon_url, sort_order) VALUES
                                                                                           (1, 'github', 'https://github.com/contact', 1),
                                                                                           (1, 'twitter', 'https://twitter.com/github', 2),
                                                                                           (2, 'wechat', 'https://juejin.cn/wechat', 1),
                                                                                           (2, 'qq', 'https://juejin.cn/qq', 2),
                                                                                           (3, 'twitter', 'https://twitter.com/v2ex', 1),
                                                                                           (4, 'stackexchange', 'https://stackexchange.com', 1),
                                                                                           (5, 'weibo', 'https://weibo.com/zhihu', 1),
                                                                                           (5, 'wechat', 'https://www.zhihu.com/wechat', 2);
