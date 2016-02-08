@echo off
if "%~1" neq "" java FindImageInImage %*
if "%~1" neq "" goto :eof

::java FindImageInImage test_image_sub_1px_diff.png test_image.png
::java FindImageInImage test_image_sub.png test_image.png
::java FindImageInImage --allowed-pixel-fails-percent 1 test_image_sub_1px_diff.png test_image.png
::java FindImageInImage --allowed-pixel-color-difference 0 --allowed-pixel-fails-percent 50 test_image_sub_small_color_diffs.png test_image_sub.png

java FindImageInImage --allowed-pixel-color-difference 150 --allowed-pixel-fails-percent 24.5 test_image_sub_small_color_diffs.png test_image_sub.png

java FindImageInImage --allowed-pixel-color-difference 150 --allowed-pixel-fails-percent 24.5 test_image_sub_small_color_diffs.png test_image.png
