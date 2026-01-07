#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简单的测试脚本
用于测试 Java 调用 Python 是否正常
"""

import sys
import time

def main():
    print("=" * 50)
    print("Hello from Python!")
    print("=" * 50)
    
    # 打印 Python 版本
    print(f"Python 版本: {sys.version}")
    
    # 打印参数
    print(f"接收到的参数数量: {len(sys.argv) - 1}")
    if len(sys.argv) > 1:
        print("参数列表:")
        for i, arg in enumerate(sys.argv[1:], 1):
            print(f"  参数{i}: {arg}")
    
    # 模拟一些计算
    print("\n开始计算...")
    for i in range(5):
        print(f"进度: {(i+1)*20}%")
        time.sleep(0.5)
    
    print("\n计算完成!")
    print("=" * 50)
    
    # 正常退出（退出码 0 表示成功）
    return 0

if __name__ == "__main__":
    exit_code = main()
    sys.exit(exit_code)

