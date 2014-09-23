create table stock.bualuang_quote_daily (symbol varchar(24) not null comment '', date date not null comment '', open double precision comment '', high double precision comment '', low double precision comment '', close double precision comment '', volume bigint comment '', value double precision comment '', primary key (symbol, date), unique (symbol, date)) comment='';
create table stock.set_broker (extend_id integer not null comment '', username varchar(10) not null comment '', password varchar(24) comment '', primary key (extend_id, username), unique (extend_id, username)) comment='';
create table stock.set_company (symbol varchar(24) not null unique comment '', market_id smallint comment '', industry_id smallint comment '', sector_id smallint comment '', set_50 boolean not null comment '', set_100 boolean not null comment '', set_hd boolean not null comment '', name_th varchar(255) comment '', name_en varchar(255) comment '', website varchar(255) comment '', last_update date comment '', primary key (symbol), unique (symbol)) comment='';
create table stock.set_extends_broker (id integer not null unique comment '', classname varchar(255) comment '', primary key (id), unique (id)) comment='';
create table stock.set_industry (market_id smallint not null comment '', industry_id smallint not null comment '', name_th varchar(255) comment '', name_en varchar(255) comment '', primary key (market_id, industry_id), unique (market_id, industry_id)) comment='';
create table stock.set_market (market_id smallint not null unique comment '', name varchar(255) comment '', primary key (market_id), unique (market_id)) comment='';
create table stock.set_sector (market_id smallint not null comment '', industry_id smallint not null comment '', sector_id smallint not null comment '', name_th varchar(255) comment '', name_en varchar(255) comment '', primary key (market_id, industry_id, sector_id), unique (market_id, industry_id, sector_id)) comment='';
create table stock.settrade_market (name varchar(24) not null comment '', date datetime not null comment '', last double precision comment '', change_prior double precision comment '', high double precision comment '', low double precision comment '', volume bigint comment '', value double precision comment '', primary key (name, date), unique (name, date)) comment='';
create table stock.settrade_quote (symbol varchar(24) not null comment '', date datetime not null comment '', open double precision comment '', high double precision comment '', low double precision comment '', last double precision comment '', change_prior double precision comment '', bid double precision comment '', bid_volume bigint comment '', offer double precision comment '', offer_volume bigint comment '', volume bigint comment '', value double precision comment '', primary key (symbol, date), unique (symbol, date)) comment='';
