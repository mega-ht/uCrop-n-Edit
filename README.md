[![](https://jitpack.io/v/Frosch2010/uCrop-n-Edit.svg)](https://jitpack.io/#Frosch2010/uCrop-n-Edit)
# uCrop'n'Edit - Image Cropping and Editing Library for Android

This repository is a fork of <a href="https://github.com/krokyze/uCrop-n-Edit">uCrop'n'Edit</a>, which is in turn a fork of <a href="https://github.com/Yalantis/uCrop">uCrop</a>. I fixed some bugs, updated the dependencies and gradle, and converted much of the code to Kotlin.

## Features

<a href="https://github.com/krokyze/uCrop-n-Edit">uCrop'n'Edit</a> extends <a href="https://github.com/Yalantis/uCrop">uCrop</a> by adding the ability to change Brightness, Contrast, Saturation, and Sharpness of images.

<p align="center">
  <img src="preview.gif" width="320" height="560">
</p>

## Usage

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
	        implementation 'com.github.Frosch2010:uCrop-n-Edit:3.0.2'
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


### onActivityResult (is deprecated)


3. The uCrop configuration is created using the builder pattern.

	```java
    UCrop.of(sourceUri, destinationUri)
        .withAspectRatio(16, 9)
        .withMaxResultSize(maxWidth, maxHeight)
        .start(context);
    ```


4. Override `onActivityResult` method and handle uCrop result.

    ```java
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }
    ```

For more information see uCrop: <a href="https://github.com/Yalantis/uCrop#usage">Usage</a>

## Feedback and Contributions

If you have any ideas for uCrop'n'Edit, feel free to let me know. I will try my best to keep this up to date. If you find any bugs, please add a new issue.

## Changelog

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
