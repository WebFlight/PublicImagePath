# Mendix PublicImagePath module

![DynamicChangeObject logo][1]

Welcome to the Mendix *PublicImagePath* module. This module makes it possible to expose images (using request handler /images) to a public URL (similar to the REST Services module when exposing images using a microflow service definition), but with correct response headers:
* Cache-Control
* Content-Length
* Content-Type (MIME-type dynamically derived from the image byte stream)
* ETag (MD5 hash)
* Expires
* Last-Modified

This enables the use of Content Delivery Networks for image distribution that do not accept octec-stream MIME-types. In addition, user experience will be improved when browsers load images from cache when correct headers are returned.

# Table of Contents

* [Getting Started](#getting-started)
* [Examples](#examples)
* [Development Notes](#development-notes)

# Getting Started

* Install the *PublicImagePath* module from the App Store.
* Create ImageServiceDefinition objects in the After Startup microflow for each image endpoint with the following attributes:
	* Include the path with parameters in curly braces: for instance '/logos/{FileName}'.
	* Include the full microflow name that exposes the image: for instance 'MyFirstModule.ExposeImageMicroFlow'.
	* The image microflow may contain one input Mendix object, which is specified in the domain model. The parameters in the path (in curly braces) will be mapped on the input object attributes with the same name. In the previous example the parameter was called FileName.
* Include the StartPublicImagePath Java action in the After Startup microflow after creation of the ImageServiceDefinition objects.
* Optionally apply the before commit microflow BCO_UrlFriendlyName to all image entities to make sure all file names are unique and URL-safe.
* The images are available using the following path: HOST/images/{Defined path}. In this example: {HOST}/images/logos/{FileName}.

# Examples

## Startup microflow

Example of a startup microflow, which creates the image service defintions and initializes the PublicImagePath (/images) request handler:

![Startup microflow example][2]

Example of an image service definition:

![Image service definition example][3]

## Expose an image

Example of a microflow with an input object that exposes an image:

![Image expose microflow][4]

The entity that is used for parameter mapping for the image expose microflow:

![Image expose microflow][5]

# Development notes

* For contributions: fork this repository, make changes, fix/add unit tests in dynamicchangeobject.tests package and issue pull request.
* Security is not implemented yet, exposed images are publicly available.
* Filenames should be URL-safe to prevent issues with URL encoding.


[1]: https://github.com/WebFlight/PublicImagePath/blob/master/docs/PublicImagePath.PNG
[2]: https://github.com/WebFlight/PublicImagePath/blob/master/docs/Capture_StartupMicroflow.PNG
[3]: https://github.com/WebFlight/PublicImagePath/blob/master/docs/Capture_CreateImageServiceDefinition.PNG
[4]: https://github.com/WebFlight/PublicImagePath/blob/master/docs/Capture_ExposeImageMicroflow.PNG
[5]: https://github.com/WebFlight/PublicImagePath/blob/master/docs/Capture_InputParameter.PNG