# Portal

[![Build Status](https://travis-ci.com/InformaticsMatters/portal.svg?branch=master)](https://travis-ci.com/InformaticsMatters/portal)
![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/informaticsmatters/portal?include_prereleases)

The Squonk Portal

## Releasing new images
The Travis CI/CD framework looks after automated builds of the portal.
Every change to the **master** branch will result in a new `squonk/portal:latest`
image on Docker Hub.

To build official (tagged) images simply tag the appropriate GitHub repository
commit and Travis will do the rest. We follow the [semantic] versioning style
and generally permit formal (official) tags of the form `MAJOR.MINOR.PATCH`
and `alpha`, `beta` and `rc` pre-release tags. The following illustrates
tags you might expect to find: -

-   `1.0.0`
-   `1.0.1-alpha.1`
-   `1.0.1-beta.1`
-   `1.0.1-rc.1`

Docker images for tags will use the tag as the image tag, so tag `1.0.1-rc.1`
will result in `squonk.portal:1.0.1-rc.1`.

In summary: -

1.  Every change to master results in a new `squonk/portal:latest` image
2.  Every tag results in a new `squonk/portal:${TRAVIS_TAG}` image
    and a new `squonk/portal:latest` image
3.  Every non-pre-release tag results in a new `squonk.portal:stable` image

---

[semantic]: https://semver.org
