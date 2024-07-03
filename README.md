[![](https://jitpack.io/v/jens-muenker/uCrop-n-Edit.svg)](https://jitpack.io/#jens-muenker/uCrop-n-Edit) [![](https://jitpack.io/v/jens-muenker/uCrop-n-Edit/month.svg)](https://jitpack.io/#jens-muenker/uCrop-n-Edit)
# uCrop'n'Edit - Image Cropping and Editing Library for Android

This repository is a fork of <a href="https://github.com/krokyze/uCrop-n-Edit">uCrop'n'Edit</a>, which is in turn a fork of <a href="https://github.com/Yalantis/uCrop">uCrop</a>. I fixed some bugs, updated the dependencies and gradle, and converted much of the code to Kotlin. Furthermore, I added the option to use an ActivityResultLauncher instead of onActivityResult which is deprecated. In addition, in the changelogs you can see all other features I added.

# Features

<a href="https://github.com/krokyze/uCrop-n-Edit">uCrop'n'Edit</a> extends <a href="https://github.com/Yalantis/uCrop">uCrop</a> by adding the ability to change Brightness, Contrast, Saturation, and Sharpness of images. I added a few more things. You can see these in the changelog.

<p align="center">
  <img src="preview.gif" width="320" height="560">
</p>

# Usage

1. Include the library as a local library project in your build.gradle:

    ```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
	...
	
	dependencies {
	        implementation 'com.github.jens-muenker:uCrop-n-Edit:3.0.7'
	}
    ```

2. Add UCropActivity into your AndroidManifest.xml

    ```
    <activity
        android:name="com.yalantis.ucrop.UCropActivity"
        android:screenOrientation="portrait"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar"/>
    ```
    
    
### ActivityResultLauncher


3. Create an ActivityResultLauncher and handle uCrop result.

    ```kotlin
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri = UCrop.getOutput(result.data!!)!!
        }
    }
    ```

4. The uCrop configuration is created using the builder pattern.

    ```kotlin
    UCrop.of(sourceUri, destinationUri)
        .withAspectRatio(16, 9)
        .withMaxResultSize(maxWidth, maxHeight)
        .start(context, activityResultLauncher)
    ```

[Here](https://github.com/jens-muenker/uCrop-n-Edit/blob/master/UCrop-Options.md) you can find possible options.

# Feedback and Contributions

If you have any ideas for uCrop'n'Edit, feel free to let me know. I will try my best to keep this up to date. If you find any bugs, please add a new issue.

# Changelog

**3.0.7**

- added bundle proguard rules in AAR (https://github.com/Yalantis/uCrop/pull/881)
- updated CImg to 3.4.0
- set minSdkVersion to 21
- removed support for armeabi
- updated dependencies and gradle

**3.0.6**

- removed onActivityResult (https://github.com/jens-muenker/uCrop-n-Edit/issues/6)
- updated dependencies and gradle (https://github.com/jens-muenker/uCrop-n-Edit/issues/3)
- set sourceCompatibility and targetCompatibility to java version 21

**3.0.5**

- added support for destination uri as a content provider uri (https://github.com/Yalantis/uCrop/pull/886)
- updated dependencies and gradle

**3.0.4**

- updated dependencies
- added Vietnamese localization (https://github.com/Yalantis/uCrop/pull/845)
- updated French localization (https://github.com/Yalantis/uCrop/pull/838)
- updated Italian localization (https://github.com/Yalantis/uCrop/pull/878)
- updated German localization

**3.0.3**

- transleted sample to kotlin
- added option to set titlebar text gravity and size (https://github.com/Yalantis/uCrop/pull/856)

**3.0.2**

- downgraded gradle to 7.4 (Because jitpack.io does not support higher versions at the moment)

**3.0.1**

- updated to newest gradle version
- added option to pass an extra-bundle to the intent. This will be passed back after the cropping.

**3.0.0** - Based on <a href="https://github.com/krokyze/uCrop-n-Edit">uCrop'n'Edit</a> 2.2.8 and <a href="https://github.com/Yalantis/uCrop">uCrop</a> 2.2.8:

- updated to newest gradle version
- updated libarys to newest versions
- added an AcivityResultLauncher for UCrop
- fixed a bug mentioned in the <a href="https://github.com/Yalantis/uCrop">uCrop</a> pull requests (https://github.com/Yalantis/uCrop/pull/809)


# License

This software is licensed under the Apache License, Version 2.0. See the <a href="https://www.apache.org/licenses/LICENSE-2.0">LICENSE</a> file for details.

    Copyright 2023, Jens Münker

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

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
