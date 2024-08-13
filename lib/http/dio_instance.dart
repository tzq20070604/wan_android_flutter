import 'dart:collection';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:dio/browser.dart';
import 'package:dio_cookie_manager/dio_cookie_manager.dart';
import 'package:wan_android_flutter/http/interceptor/print_log_interceptor.dart';
import 'package:wan_android_flutter/http/interceptor/rsp_interceptor.dart';
import 'http_method.dart';
import 'interceptor/cookie_interceptor.dart';

class DioInstance {
  static DioInstance? _instance;

  DioInstance._internal();

  static DioInstance instance() {
    return _instance ??= DioInstance._internal();
  }

  final _dio = kIsWeb ? DioForBrowser() : Dio();
  final _defaultTimeout = const Duration(seconds: 30);
  var _inited = false;

  void initDio(
      {required String baseUrl,
      String? method = HttpMethod.GET,
      Duration? connectTimeout,
      Duration? receiveTimeout,
      Duration? sendTimeout,
      ResponseType? responseType = ResponseType.json,
      String? contentType,
      Map<String, dynamic> extra = const {"withCredentials": true}}) async {
    _dio.options = BaseOptions(
        method: method,
        baseUrl: baseUrl,
        connectTimeout: connectTimeout ?? _defaultTimeout,
        receiveTimeout: receiveTimeout ?? _defaultTimeout,
        sendTimeout: sendTimeout ?? _defaultTimeout,
        extra: extra,
        responseType: responseType,
        contentType: contentType);
    if (!kIsWeb) {
      _dio.interceptors.add(CookieInterceptor());
      final cookieJar = CookieJar();
      _dio.interceptors.add(CookieManager(cookieJar));
    }
    _dio.interceptors.add(PrintLogInterceptor());
    _dio.interceptors.add(RspInterceptor());
    _inited = true;
  }

  ///get请求方式
  Future<Response> get({
    required String path,
    Map<String, dynamic>? param,
    Options? options,
    CancelToken? cancelToken,
  }) async {
    if (!_inited) {
      throw Exception("you should call initDio() first!");
    }
    return await _dio.get(path,
        queryParameters: param,
        options: options ??
            Options(
              method: HttpMethod.GET,
              receiveTimeout: _defaultTimeout,
              sendTimeout: _defaultTimeout,
            ),
        cancelToken: cancelToken);
  }

  ///post请求方式
  Future<Response> post(
      {required String path,
      Object? data,
      Map<String, dynamic>? queryParameters,
      Options? options,
      CancelToken? cancelToken}) async {
    if (!_inited) {
      throw Exception("you should call initDio() first!");
    }
    return await _dio.post(path,
        data: data,
        queryParameters: queryParameters,
        cancelToken: cancelToken,
        options: options ??
            Options(
              method: HttpMethod.POST,
              receiveTimeout: _defaultTimeout,
              sendTimeout: _defaultTimeout,
            ));
  }

  void changeBaseUrl(String baseUrl) {
    _dio.options.baseUrl = baseUrl;
  }
}
