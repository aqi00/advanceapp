cmake_minimum_required(VERSION 3.6) # 指定CMake的最低要求版本号

set(target lamemp3) # 设置环境变量的名称（target）及其取值（lamemp3）
project(${target}) # 指定项目的名称

aux_source_directory(libmp3lame SRC_LIST) # 查找在某个路径下的所有源文件

add_library(${target} SHARED lame-lib.cpp ${SRC_LIST}) # 生成动态库（共享库）（库文件的扩展名为so）
