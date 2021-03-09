# Portal

[![build](https://github.com/InformaticsMatters/portal/actions/workflows/build.yaml/badge.svg)](https://github.com/InformaticsMatters/portal/actions/workflows/build.yaml)
[![build latest](https://github.com/InformaticsMatters/portal/actions/workflows/build-latest.yaml/badge.svg)](https://github.com/InformaticsMatters/portal/actions/workflows/build-latest.yaml)
[![build tag](https://github.com/InformaticsMatters/portal/actions/workflows/build-tag.yaml/badge.svg)](https://github.com/InformaticsMatters/portal/actions/workflows/build-tag.yaml)
[![build stable](https://github.com/InformaticsMatters/portal/actions/workflows/build-stable.yaml/badge.svg)](https://github.com/InformaticsMatters/portal/actions/workflows/build-stable.yaml)

![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/informaticsmatters/portal?include_prereleases)

The Squonk Portal

## Releasing new images
The GitHub Actions CI/CD framework looks after automated builds of the portal.
Every change to the **master** branch will result in a new `squonk/portal:latest`
image on Docker Hub.

To build official (tagged) images simply tag the appropriate GitHub repository
commit and GitHub will do the rest. We follow the [semantic] versioning style
and generally permit formal (official) tags of the form `MAJOR.MINOR.PATCH`
and `alpha`, `beta` and `rc` pre-release tags. The following illustrates
tags you might expect to find: -

-   `1.0.0`
-   `1.0.1-alpha.1`
-   `1.0.1-beta.1`
-   `1.0.1-rc.1`

>   Docker images for tags will use the tag as the image tag, so tagging
    the repository with `1.0.1-rc.1` will result in the image
    `squonk.portal:1.0.1-rc.1`.

In summary: -

1.  Every change to master results in a new `squonk/portal:latest` image
2.  Every tag results in a new `squonk/portal:${TAG}` image
3.  Every *official* tag results in a new `squonk/portal:stable` image

---

[semantic]: https://semver.org
