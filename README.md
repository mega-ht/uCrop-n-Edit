## uCrop'n'Edit - Image Cropping + Editing Library for Android

#### Using the source code of "<a href="https://github.com/Yalantis/uCrop">uCrop</a>", I’ve added four new features - ability to change Brightness, Contrast, Saturation and Sharpness.

<p align="center" style="background-color:#ededed">
  <img src="preview.gif" width="320" height="560">
</p>

## Usage

1. Include the library as local library project.

	```
	allprojects {
	   repositories {
	      jcenter()
	      maven { url "https://jitpack.io" }
	   }
	}
	```

    ``` implementation 'com.github.krokyze:ucropnedit:2.2.6' ```
    
2. To use uCrop’n’Edit, you can follow the exact same methods as for uCrop: <a href="https://github.com/Yalantis/uCrop#usage">Usage</a>

If you have any interesting ideas for uCrop’n’Edit do not be afraid to let me know. You can easily leave a request and I will be happy to take a look at the viability of your proposal. I’ll be more than happy to try and add some new features to the library, if I think they can make it even better!

#### Please keep in mind that most of the work is developed by Yalantis, so if you find any bugs that’s not related to brightness, contrast, saturation or sharpness, you should add a new issue to their <a href="https://github.com/Yalantis/uCrop/issues">Github page</a> and as they will push update, I will pull it to make sure everything works as best as it can.

Massive thanks to guys from Yalantis for open sourcing this great library!

## Changelog (<a href="https://github.com/Yalantis/uCrop#changelog">uCrop Changelog</a>)

### Version 2.2.6 (based on uCrop 2.2.6)

  * Merged new version from uCrop.

### Version 2.2.5 (based on uCrop 2.2.4)

  * Merged new version from uCrop.

### Version 2.2.4 (based on uCrop 2.2.3)

  * Merged new version from uCrop.

### Version 2.2.3 (based on uCrop 2.2.2)

  * Merged new version from uCrop.

### Version 2.2.2 (based on uCrop 2.2.1)

  * Saturation feature
  * Sharpness feature (supported for devices with >= API 17)

### Version: 2.2.1

  * Brightness feature
  * Contrast feature

## License

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
