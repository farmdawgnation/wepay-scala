---
title: WePay-Scala
layout: default
---

# WePay-Scala

**WePay-Scala** is a Scala library for talking to [WePay](http://wepay.com) to process payments for the customers
who use your website. Built on databinder-dispatch, and some of the components of the superb Lift Framework,
WePay-Scala is designed to get to up and rolling in the easiest way possible. We've taken a play out of the
lift-mongodb handbook for how we handle our management of instances of objects on the WePay system. (If you
can't tell, we built this in an application that's using Lift already, but you're not required to do so -
it may just take a bit for the mechanics to become familiar.)

This library was originally developed for my former employer, [OpenStudy](http://openstudy.com), for use on their
now-defunct learning-goal centric companion site OpenStudy Catapult. If you're looking for a easy, intuitive way
to process payments for your app while minimizing your PCI requirements, read on, dear reader, read on.

## How do I get started?

The first step is to add the library to your build.sbt file. You can do that by doing the following:

  libraryDependencies += "me.frmr.wepay-scala" %% "wepay-scala" % "0.9.1"

Now when you compile your project the WePay-Scala library should be in the classpath. All you should have to do is import the `me.frmr.wepay._` and `me.frmr.wepay.api_` packages. After you've got the library, you'll want to try getting started by running some operations on the API. The [GitHub Project Page](htttp://github.com/farmdawgnation/wepay-scala) has a decent description of how to do this for the time being. More work is forthcoming on that front, of course. Also of interest is the (now much more complete) [API Documentation](http://wepay-scala.frmr.me/api/0.8.3), which is also still a Work-in-Progress, but should give you a decent starting point.

As always, if you find something amiss, [file an issue and let me know](http://github.com/farmdawgnation/wepay-scala/issues).

## Defining the Required Properties

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

## Start Your Engines

For brevety sake, I'm not going to go through the process of explaining WePay's entire API flow. Besides,
[their documentation](http://wepay.com/developer) does a much better job of that than I could right now.
In the following sections, I'm going to show you how to do a few common tasks.

### Authorize after successful OAuth

After WePay kicks the user back and passes the code you need to retrieve their token, you can retrieve the
actual token by running `WePay.retrieveToken`.

  val code = ... //Do something that parses out the code from the URL
  val wePayToken = WePay.retrieveToken(code)

This produces an instance of the WePayToken case class that you can use to interact with WePay. When you need
to do an operation as that user on WePay's system, you'll need to define an implicit variable named authorizationToken
that contains the object returns from retrieveToken. Like so:

  implicit val authorizationToken = wePayToken //... or whatever else you need to do to get it
  Checkout(....).save
  // and so on...

### Create a checkout object.

You should read up on WePay's developer documentation for full details, but in general you'll need to
create a Checkout object then persist that to WePay to start your Checkout flow. After persisting, you'll
be provided with a CheckoutResponse instance that contains the URI you need to redirect your users to.

So, you could so something like this.

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

You should check out the full [/checkout reference](https://www.wepay.com/developer/reference/checkout)
for more details on this. (See what I did there?)

#### Retrieve information on a Checkout

So, after creating a checkout object that is associated with a purchase on your store, you can and
should store the `checkout\_id` field in your database. Later you'll want to use this to retrive
information about the checkout (specifically if you're going to be processing IPN messages from
WePay. Naturally, you can use this id number to retrieve information on your checkout after it
is created on WePay's system like so:

  val checkoutInstance = Checkout.find(checkoutIdNumber)

  checkoutInstance.foreach { checkout =>
    // Do something with the checkout
  }

#### Handling Errors

Sometimes, things go wrong. WePay-Scala makes heavy use of the Box class from Lift to account for
these situations without utilizing exceptions. **Learn to love the box.** The Box is your friend,
even though it is almost guaranteed that if you havent used it before it will make your head
explode. Most operations on the WePay library return a Box of some kind. That Box can either be
a Full containing an object you want to pull apart (e.g. Checkout/CheckoutResponse), an Empty
that means nothing was found like what you were looking for, or a Failure with a WePayError
inside.
