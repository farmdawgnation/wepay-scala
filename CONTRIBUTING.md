# Contributing to WePay Scala

Hey, nice to meet you. I'm glad to hear you're interested in contributing to the
project. Here are some basic rules for the road to ensure that your contribution
fits in nicely.

## Filing Bug Reports

If you file a bug report, please be sure to indicate what version of the library you're
using. Please also indicate what version of Lift you're running and what error message,
if any, was produced by WePay.

Also, please don't file your bug and abandon it. We'll likely have questions for you
after your initial bug report so please be so kind as to respond in a timely manner.
We don't like bugs any more than you and the quicker we can figure out what is causing
them, the quicker we can nuke them.

## Contributing Code

There are a few basic rules of the road for contributing code in Pull Requests.

### Code Style

* Indentation is with two spaces. If I see tabs I will dropkick you into oblivion.
* Variable names are in camelCase (the one exception currently being things that are
serialized to JSON - but we're going to be fixing that soon).
* Let the type infrencer do as much of the work as possible. Only use explicit types
when neccicary.

### Developing your Contribution

* After forking the WePay-Scala repository, work on your contribution in a topic-based
branch so that master and other branches from my repository can stay in sync if I decide
to do a git squash or something similar.
* Please ensure the first line of your commit message is at most 72 characters. You can
have an expanded message after a blank line, but please keep the width of those line to
72 characters as well. If you use VIM to write your commit messages, please consider using
[vim-fugative](https://github.com/tpope/vim-fugitive).
