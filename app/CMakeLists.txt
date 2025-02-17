cmake_minimum_required(VERSION 3.10.2)

project("musicplatform")

add_library(
        format-time-lib
        SHARED
        format-time.c
)

find_library(
        log-lib
        log
)

target_link_libraries(
        format-time-lib
        ${log-lib}
)
