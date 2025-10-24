create table public."user"
(
    id            bigserial
        primary key,
    user_account  varchar(256)                                               not null,
    user_password varchar(512)                                               not null,
    username      varchar(256)             default '游客'::character varying not null,
    user_avatar   varchar(1024)            default 'https://th.bing.com/th/id/R.54a295a86f04aaf12f1285d4e00fd6be?rik=QAdEADu3LNh9Hg&pid=ImgRaw&r=0'::character varying,
    user_role     varchar(256)             default 'user'::character varying not null,
    create_time   timestamp with time zone default CURRENT_TIMESTAMP         not null,
    update_time   timestamp with time zone default CURRENT_TIMESTAMP         not null,
    is_delete     smallint                 default 0                         not null
);

comment on table public."user" is '用户';

comment on column public."user".id is 'id';

comment on column public."user".user_account is '账号';

comment on column public."user".user_password is '密码';

comment on column public."user".username is '用户昵称';

comment on column public."user".user_avatar is '用户头像';

comment on column public."user".user_role is '用户角色：user/admin/ban';

comment on column public."user".create_time is '创建时间';

comment on column public."user".update_time is '更新时间';

comment on column public."user".is_delete is '是否删除';

alter table public."user"
    owner to burger_blog;

create index idx_useraccount
    on public."user" (user_account);

create table public.tag
(
    id          bigserial
        primary key,
    name        varchar(50) not null
        unique,
    description text,
    color       varchar(7) default '#1890ff'::character varying,
    create_time timestamp  default CURRENT_TIMESTAMP,
    update_time timestamp  default CURRENT_TIMESTAMP
);

alter table public.tag
    owner to burger_blog;

create table public."column"
(
    id          bigserial
        primary key,
    name        varchar(100) not null,
    description text,
    cover_image varchar(500),
    create_time timestamp default CURRENT_TIMESTAMP,
    update_time timestamp default CURRENT_TIMESTAMP
);

comment on column public."column".cover_image is '封面';

alter table public."column"
    owner to burger_blog;

create table public.article
(
    id              bigserial
        primary key,
    title           varchar(200) default ''::character varying not null,
    content         text,
    excerpt         varchar(512) default ''::character varying not null,
    cover_image     varchar(500) default ''::character varying not null,
    status          smallint     default 0                     not null,
    read_time       integer      default 0                     not null,
    views           integer      default 0                     not null,
    seo_title       varchar(100) default ''::character varying not null,
    seo_description varchar(256) default ''::character varying not null,
    seo_keywords    varchar(256) default ''::character varying not null,
    published_time  timestamp    default CURRENT_TIMESTAMP     not null,
    create_time     timestamp    default CURRENT_TIMESTAMP     not null,
    update_time     timestamp    default CURRENT_TIMESTAMP     not null
);

alter table public.article
    owner to burger_blog;

create table public.article_tag
(
    id          bigserial
        primary key,
    article_id  bigint    default 0 not null,
    tag_id      bigint    default 0 not null,
    create_time timestamp default CURRENT_TIMESTAMP
);

alter table public.article_tag
    owner to burger_blog;

create table public.article_column
(
    id          bigserial
        primary key,
    article_id  bigint    default 0 not null,
    column_id   bigint    default 0 not null,
    create_time timestamp default CURRENT_TIMESTAMP
);

alter table public.article_column
    owner to burger_blog;

create table public.comment
(
    id           bigserial
        primary key,
    article_id   bigint       default 0                         not null,
    parent_id    bigint       default 0                         not null,
    nickname     varchar(100) default '游客'::character varying not null,
    user_email   varchar(100) default ''::character varying     not null,
    user_website varchar(255) default ''::character varying     not null,
    user_avatar  varchar(500) default ''::character varying     not null,
    content      varchar(500) default ''::character varying     not null,
    ip_address   inet,
    user_agent   text,
    create_time  timestamp    default CURRENT_TIMESTAMP,
    update_time  timestamp    default CURRENT_TIMESTAMP
);

alter table public.comment
    owner to burger_blog;

create table public.site_setting
(
    id            integer     default nextval('site_settings_id_seq'::regclass) not null
        constraint site_settings_pkey
            primary key,
    setting_key   varchar(100)                                                  not null
        constraint site_settings_setting_key_key
            unique,
    setting_value text,
    setting_type  varchar(20) default 'string'::character varying
        constraint site_settings_setting_type_check
            check ((setting_type)::text = ANY
                   ((ARRAY ['string'::character varying, 'number'::character varying, 'boolean'::character varying, 'json'::character varying])::text[])),
    description   text,
    create_time   timestamp   default CURRENT_TIMESTAMP,
    update_time   timestamp   default CURRENT_TIMESTAMP
);

alter table public.site_setting
    owner to burger_blog;

create table public.article_view_record
(
    id         serial
        primary key,
    article_id bigint    default 0 not null,
    user_id    bigint    default 0 not null,
    ip_address inet,
    user_agent text,
    viewed_at  timestamp default CURRENT_TIMESTAMP
);

alter table public.article_view_record
    owner to burger_blog;

create table public.friend_link
(
    id           serial
        primary key,
    name         varchar(100)                                           not null,
    description  varchar(200)             default ''::character varying,
    avatar       varchar(500)             default ''::character varying not null,
    url          varchar(500)             default ''::character varying not null,
    is_special   boolean                  default false,
    status_label varchar(50),
    sort_order   integer                  default 0,
    status       integer                  default 1,
    created_time timestamp with time zone default CURRENT_TIMESTAMP,
    updated_time timestamp with time zone default CURRENT_TIMESTAMP
);

comment on table public.friend_link is '友情链接主表';

comment on column public.friend_link.name is '网站名称';

comment on column public.friend_link.description is '网站描述';

comment on column public.friend_link.avatar is '头像URL';

comment on column public.friend_link.url is '网站链接';

comment on column public.friend_link.is_special is '是否为特殊卡片';

comment on column public.friend_link.status_label is '状态标签(如PREMIUM, VIP等)';

comment on column public.friend_link.sort_order is '排序权重';

comment on column public.friend_link.status is '状态: 1-正常, 0-禁用, -1-删除';

alter table public.friend_link
    owner to burger_blog;

create table public.social_link
(
    id             serial
        primary key,
    friend_link_id integer     not null,
    icon_type      varchar(20) not null,
    icon_url       varchar(500),
    sort_order     integer                  default 0,
    created_time   timestamp with time zone default CURRENT_TIMESTAMP,
    updated_time   timestamp with time zone default CURRENT_TIMESTAMP
);

comment on table public.social_link is '社交链接表';

comment on column public.social_link.icon_type is '图标类型: qq, wechat, heart, star等';

comment on column public.social_link.icon_url is '社交链接URL';

comment on column public.social_link.sort_order is '排序权重';

create table public.setting_config
(
    id                  bigserial
        primary key,
    site_name           varchar(255) default ''::character varying not null,
    site_description    text,
    site_keywords       varchar(500),
    site_logo           varchar(500),
    favicon             varchar(500),
    about_title         varchar(255),
    about_content       text,
    about_image         varchar(500),
    seo_title           varchar(255),
    seo_description     text,
    seo_keywords        varchar(500),
    github_url          varchar(500),
    twitter_url         varchar(500),
    linkedin_url        varchar(500),
    email_contact       varchar(255),
    wechat_qr_url       varchar(500),
    wechat_pay_qr_url   varchar(500),
    alipay_qr_url       varchar(500),
    enable_comments     boolean      default true,
    enable_search       boolean      default true,
    enable_dark_mode    boolean      default true,
    articles_per_page   integer      default 10,
    google_analytics_id varchar(100),
    baidu_analytics_id  varchar(100),
    create_time         timestamp    default CURRENT_TIMESTAMP,
    update_time         timestamp    default CURRENT_TIMESTAMP,
    ai_chat_shortcut    varchar(200)
);

comment on column public.setting_config.ai_chat_shortcut is 'AI弹窗快捷键';

alter table public.setting_config
    owner to burger_blog;

create index idx_site_name
    on public.setting_config (site_name);

create index idx_update_time
    on public.setting_config (update_time);


