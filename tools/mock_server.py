#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
千年诗境 · 本地预览服务器
用法：python mock_server.py
然后浏览器打开 http://localhost:8000

功能：
1. 直接预览 frontend/ 下的所有页面
2. 提供 /api/* 接口，默认返回模拟数据
3. 如果 output/ 目录下有 MapReduce 跑出的 part-r-00000，自动读取真实数据
"""
import http.server
import socketserver
import json
import os
import re

PORT = 8000
FRONTEND_DIR = os.path.join(os.path.dirname(__file__), '..', 'frontend')
OUTPUT_DIR = os.path.join(os.path.dirname(__file__), '..', 'mr', 'member5', 'output')

# 模拟数据
MOCK = {
    '/api/category-count': {
        '古体诗': 52341, '近体诗': 38920, '词': 8200,
        '绝句': 6500, '律诗': 4200, '乐府': 1800, '其他': 1500
    },
    '/api/length-range': {
        '0-20': 12000, '21-50': 45000, '51-100': 32000, '101-200': 15000, '200+': 5643
    }
}

def read_mr_result(filename):
    """读取 MapReduce 输出的 part-r-00000，转成 dict"""
    path = os.path.join(OUTPUT_DIR, filename, 'part-r-00000')
    if not os.path.exists(path):
        return None
    result = {}
    with open(path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            parts = re.split(r'\t', line)
            if len(parts) >= 2:
                try:
                    result[parts[0]] = int(parts[-1])
                except ValueError:
                    pass
    return result

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=FRONTEND_DIR, **kwargs)

    def do_GET(self):
        if self.path.startswith('/api/'):
            self.send_response(200)
            self.send_header('Content-Type', 'application/json; charset=utf-8')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            data = MOCK.get(self.path, {'msg': '接口暂未实现'})
            self.wfile.write(json.dumps(data, ensure_ascii=False).encode('utf-8'))
        else:
            super().do_GET()

    def log_message(self, format, *args):
        pass  # 安静模式

if __name__ == '__main__':
    print(f"千年诗境本地预览: http://localhost:{PORT}")
    print(f"前端目录: {os.path.abspath(FRONTEND_DIR)}")
    print(f"MR输出目录: {os.path.abspath(OUTPUT_DIR)}")
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        httpd.serve_forever()
