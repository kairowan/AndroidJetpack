package com.kotlinmvvm.core.network.config

import com.kotlinmvvm.core.network.R

/**
 * @author 浩楠
 * @date 2026/7/21 09:23
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 网络配置校验错误枚举，以稳定类型和字符串资源替代散落的硬编码异常文案
 */
enum class NetworkConfigurationError(val messageResource: Int) {
    BLANK_ENDPOINT_NAME(R.string.core_network_config_blank_endpoint_name),
    INVALID_BASE_URL_HOST(R.string.core_network_config_invalid_base_url_host),
    BASE_URL_MISSING_TRAILING_SLASH(R.string.core_network_config_base_url_trailing_slash),
    BASE_URL_HAS_QUERY_OR_FRAGMENT(R.string.core_network_config_base_url_query_or_fragment),
    HTTPS_REQUIRED(R.string.core_network_config_https_required),
    INVALID_CONNECT_TIMEOUT(R.string.core_network_config_connect_timeout),
    INVALID_READ_TIMEOUT(R.string.core_network_config_read_timeout),
    INVALID_WRITE_TIMEOUT(R.string.core_network_config_write_timeout),
    INVALID_CALL_TIMEOUT(R.string.core_network_config_call_timeout),
    INVALID_CACHE_SIZE(R.string.core_network_config_cache_size),
    BLANK_USER_AGENT(R.string.core_network_config_blank_user_agent),
    EMPTY_RESOURCE_HOSTS(R.string.core_network_config_empty_resource_hosts),
    RESOURCE_UPGRADE_HOST_NOT_ALLOWED(R.string.core_network_config_upgrade_host_not_allowed),
    INVALID_RESOURCE_HTTPS_PORT(R.string.core_network_config_invalid_resource_https_port),
    INVALID_RESOURCE_HOST(R.string.core_network_config_invalid_resource_host),
    EMPTY_CONVERTER_FACTORIES(R.string.core_network_config_empty_converter_factories)
}
