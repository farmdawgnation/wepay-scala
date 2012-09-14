---
title: WePay-Scala
layout: default
---

# WePay-Scala

**WePay-Scala** is a Scala library for talking to [WePay](http://wepay.com) to process payments for the customers who use your website. Built on databinder-dispatch, and some of the components of the superb Lift Framework, WePay-Scala is designed to get to up and rolling in the easiest way possible. We've taken a play out of the lift-mongodb handbook for how we handle our management of instances of objects on the WePay system. (If you can't tell, we built this in an application that's using Lift already, but you're not required to do so - it may just take a bit for the mechanics to become familiar.)

This library was originally developed for my employer, [OpenStudy](http://openstudy.com), for use on our learning-goal centric companion site [OpenStudy Catapult](http://catapult.openstudy.com). Because the people I work for are so awesome and genuinely believe in the power of Open Source, I get to share the awesomeness with everyone!

If you're looking for a easy, intuitive way to process payments for your app while minimizing your PCI requirements, read on, dear reader, read on.

## How do I get started?

The first step is to add the library to your build.sbt file. You can do that by doing the following:

	libraryDependencies += "me.frmr.wepay-scala" %% "wepay-scala" % "0.8.2"

Now when you compile your project the WePay-Scala library should be in the classpath. All you should have to do is import the `me.frmr.wepay._` and `me.frmr.wepay.api_` packages. After you've got the library, you'll want to try getting started by running some operations on the API. The [GitHub Project Page](htttp://github.com/farmdawgnation/wepay-scala) has a decent description of how to do this for the time being. More work is forthcoming on that front, of course. Also of interest is the (now much more complete) [API Documentation](http://wepay-scala.frmr.me/api), which is also still a Work-in-Progress, but should give you a decent starting point.

## Roadmap Moving Forward

Although this library is stable and being actively used in production, there are a few things left to be done, hence the version designation < 1.0. Specificially:

1. Better, more complete documentation.
2. Unit Tests (complete with TravisCI-ness).
3. Update the databinder-dispatch used here to the latest version.
4. Support for WePay's new credit_card API endpoint.

As always, if you find something amiss, [file an issue and let me know](http://github.com/farmdawgnation/wepay-scala/issues).
