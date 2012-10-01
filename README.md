# WePay API for Scala

[![Build Status](https://secure.travis-ci.org/farmdawgnation/wepay-scala.png?branch=0.9-SNAPSHOT)](http://travis-ci.org/farmdawgnation/wepay-scala)

This project is an API for payment processing via WePay written in Scala, originally developed
for [OpenStudy](http://openstudy.com) for use in [Catapult](http://catapult.openstudy.com). The
APIs were engineered to immitate lift-mongodb as closely as possible (because let's be honest,
lift-mongodb is pretty elegant to work with).

This project is released under the terms of the Apache License 2.0.

## Getting Started

### Overview

Deploying this library in your applications consists of roughly three steps.

1. Define the required properties wherever your environment properties are defined.
2. ???
3. PROFIT!

No seriously, it really is about that simple. After you add this library to your list of dependencies
all you should be required to do is define the properties you need in your environment, import
`me.frmr.wepay._` and `me.frmr.wepay.api._` and get rolling. Some familiarity with [Lift](http://liftweb.net)
will be helpful, as we pull come components (e.g. Box) and inspriration (e.g. lift-mongodb style classes) from them.

For most operations, you'll interact with the resource classes (Account, Checkout, Preapproval, User, Withdrawal) with
the one exception being that you'll need to interact directly with the WePay singleton to complete an OAuth authentication
flow.

More details on this below.

### Requirements

There are a few requirements to using this library.

* You must be using Scala 2.9.1 or 2.9.2.
* If you are working with a Lift project, you may want to add an exclusion rule to exclude items in net.liftweb
* If you are using dispatch in your project, you must be using 0.8.8, as that's the version this library is built against.

Also worth nothing, the next release - or the one immediately after - will see us make the jump
from Dispatch 0.8.8 to 0.9.1. So if you're already using Dispatch 0.9.1, we're comin' for you. I won't
be making that jump until after the Lift 2.5-M1 release comes out though.

### Getting the Library

WePay-Scala is available in the Maven Central repository. You can use it by adding
the following line to the build.sbt file in your project.

```scala
libraryDependencies += "me.frmr.wepay-scala" %% "wepay-scala" % "0.8.3"
```

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

### Start Your Engines

For brevety sake, I'm not going to go through the process of explaining WePay's entire API flow. Besides,
[their documentatin](http://wepay.com/developer) does a much better job of that than I could right now.
In the following sections, I'm going to show you how to do a few common tasks.

#### Authorize after successful OAuth

After WePay kicks the user back and passes the code you need to retrieve their token, you can retrieve the
actual token by running `WePay.retrieveToken`.

```scala
val code = ... //Do something that parses out the code from the URL
val wePayToken = WePay.retrieveToken(code)
```

This produces an instance of the WePayToken case class that you can use to interact with WePay. When you need
to do an operation as that user on WePay's system, you'll need to define an implicit variable named authorizationToken
that contains the object returns from retrieveToken. Like so:

```scala
implicit val authorizationToken = wePayToken //... or whatever else you need to do to get it
Checkout(....).save
// and so on...
```

#### Create a checkout object.

You should read up on WePay's developer documentation for full details, but in general you'll need to
create a Checkout object then persist that to WePay to start your Checkout flow. After persisting, you'll
be provided with a CheckoutResponse instance that contains the URI you need to redirect your users to.

So, you could so something like this.

```scala
// Some awesome logic
val checkoutResponse = Checkout(1234, "Awesome Item", "GOODS", 10.0).save

{
  for {
    response <- checkoutResponse
  } yield {
    // Redirect the user to response.checkout_uri
  }
} openOr {
  // Trigger some error to the user.
}
```

You should check out the full [/checkout reference](https://www.wepay.com/developer/reference/checkout)
for more details on this. (See what I did there?)

#### Retrieve information on a Checkout

So, after creating a checkout object that is associated with a purchase on your store, you can and
should store the `checkout\_id` field in your database. Later you'll want to use this to retrive
information about the checkout (specifically if you're going to be processing IPN messages from
WePay. Naturally, you can use this id number to retrieve information on your checkout after it
is created on WePay's system like so:

```scala
val checkoutInstance = Checkout.find(checkoutIdNumber)

checkoutInstance.foreach { checkout =>
  // Do something with the checkout
}
```

#### Handling Errors

Sometimes, things go wrong. WePay-Scala makes heavy use of the Box class from Lift to account for
these situations without utilizing exceptions. **Learn to love the box.** The Box is your friend,
even though it is almost guaranteed that if you havent used it before it will make your head
explode. Most operations on the WePay library return a Box of some kind. That Box can either be
a Full containing an object you want to pull apart (e.g. Checkout/CheckoutResponse), an Empty
that means nothing was found like what you were looking for, or a Failure with a WePayError
inside.

## Who am I

My name is Matt Farmer. I'm a Software Engineer at [OpenStudy](http://openstudy.com) where we're
in the practice of building disruptive educational technology.
