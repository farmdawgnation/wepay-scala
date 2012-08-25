# WePay API for Scala

This project is an API for payment processing via WePay written in Scala, originally developed
for [OpenStudy](http://openstudy.com) for use in [Catapult](http://catapult.openstudy.com). The
APIs were engineered to immitate lift-mongodb as closely as possible (because let's be honest,
lift-mongodb is pretty elegant to work with).

## Overview

Deploying this library in your applications consists of roughly three steps.

1. Define the required properties wherever your environment properties are defined.
2. ???
3. PROFIT!

No seriously, it really is about that simple. After you add this library to your list of dependencies
all you should be required to do is define the properties you need in your environment, import
`me.frmr.wepay._` and `me.frmr.wepay.api._` and get rolling. Some familiarity with [Lift](http://liftweb.net)
will be helpful, as we pull come components (e.g. Box) and inspriration (e.g. lift-mongodb style classes) from them.

More details on this below.

## Getting Started

### Defining the Required Properties

WePay-Scala requires you to define a few properties before the WePay singleton will initialize correctly.
Specifically, those are:

* wepay.clientId - Client ID for your application, provided by WePay.
* wepay.clientSecret - Client Secret for your application, provided by WePay.
* wepay.oauthRedirectUrl - The URL in your application that your users are directed to after the OAuth flow completes.
* wepay.userAgent - Some User-Agent string that will identify your application to WePay's servers.
* wepay.oauthPermissions - A comma separated list of [permissions](https://www.wepay.com/developer/reference/permissions)
  you're requesting in your OAuth requests.

Additionally, when you're ready to go into production, you'll need to define two additional properties.

* wepay.apiEndpointBase - The API endpoint. Defaulted to stage.wepayapi.com. Set to wepayapi.com for production.
* wepay.uiEndpointBase - The UI engpoint. Defaulted to stage.wepay.com. Set to www.wepay.com for production.

## Who am I

My name is Matt Farmer. I'm a Software Engineer at [OpenStudy](http://openstudy.com) where we're
in the practice of building disruptive educational technology.
