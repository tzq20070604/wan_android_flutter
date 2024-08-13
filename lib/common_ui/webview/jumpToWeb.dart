import 'package:flutter/material.dart';
import 'dart:js' as js;
import 'package:flutter/foundation.dart' show kIsWeb;
import 'webview_page.dart';
import 'webview_widget.dart';
import '../../route/RouteUtils.dart';

void openWeb(
    {required BuildContext context,
    required String loadResource,
    WebViewType webViewType = WebViewType.URL,
    bool showTitle = true,
    String? title = ""}) {
  if (kIsWeb) {
    js.context.callMethod('open', [loadResource, '_blank']);
  } else {
    RouteUtils.push(
        context,
        WebViewPage(
            loadResource: loadResource,
            webViewType: WebViewType.URL,
            showTitle: true,
            title: title));
  }
}
