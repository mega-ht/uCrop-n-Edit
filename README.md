# uCrop'n'Edit - Image Cropping and Editing Library for Android

This repository is a fork of <a href="https://github.com/krokyze/uCrop-n-Edit">uCrop'n'Edit</a>, which is in turn a fork of <a href="https://github.com/Yalantis/uCrop">uCrop</a>. I fixed some bugs, updated the dependencies and gradle, and converted much of the code to Kotlin.

## Features

<a href="https://github.com/krokyze/uCrop-n-Edit">uCrop'n'Edit</a> extends <a href="https://github.com/Yalantis/uCrop">uCrop</a> by adding the ability to change Brightness, Contrast, Saturation, and Sharpness of images.

<p align="center">
  <img src="preview.gif" width="320" height="560">
</p>

## Usage

To use uCrop'n'Edit, include the library as a local library project in your build.gradle:
	
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
	...
	
	dependencies {
	        implementation 'com.github.Frosch2010:uCrop-n-Edit:3.0.2'
	}
	

Follow the same methods as for uCrop: <a href="https://github.com/Yalantis/uCrop#usage">Usage</a>

## Feedback and Contributions

If you have any ideas for uCrop'n'Edit, feel free to let me know. I will try my best to keep this up to date. If you find any bugs, please add a new issue.

## Changelog

**3.0.1**

- updated to newest gradle version
- added option to pass an extra-bundle to the intent. This will be passed back after the cropping.

**3.0.0** - Based on <a href="https://github.com/krokyze/uCrop-n-Edit">uCrop'n'Edit</a> 2.2.8 and <a href="https://github.com/Yalantis/uCrop">uCrop</a> 2.2.8:

- updated to newest gradle version
- updated libarys to newest versions
- added an AcivityResultLauncher for UCrop
- fixed a bug mentioned in the <a href="https://github.com/Yalantis/uCrop">uCrop</a> pull requests (<a href="https://github.com/Yalantis/uCrop/pull/809">#809</a>)


## License

This software is licensed under the Apache License, Version 2.0. See the <a href="https://www.apache.org/licenses/LICENSE-2.0">LICENSE</a> file for details.


For <a href="https://github.com/Yalantis/uCrop">uCrop</a>:

    Copyright 2017, Yalantis

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
