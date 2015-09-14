# flickicker
Flickicker

This is small app demo for flickr API. 

Test:

. Create an application that can upload photos to Flickr. -> OK

. There will be three main screens: the upload screen, the photo list

screen, and the photo viewer screen. -> OK

. User can pick pictures from Android gallery as well as taking a photo

by camera. -> OK(But rotate image is not support)

. For the photo list screen, use RecyclerView to display all your

uploaded photos on Flickr vertically. -> OK

. When user taps on a photo in the photo list screen, open up the

photo viewer screen. This photo viewer screen should enlarge the

photo and support gesture zooming. -> OK

. An option to logout. -> (OK : Please click at top-left button to show Logout option)

. Make your app feel ‘materialized’ -> I think It's OK

. Make sure your app is well-architected, we love to make our app to

be internally beautiful. -> not really but can accept

Note:

- You are allow to use third-party libraries to perform network

request. However, Please do not use any image loaders library (such

as Volley’s Image Loader, Picasso, or Universal Image Loader, no

WebView as well)

- Please push your source code to github.com or bitbucket.org and

give us the link.

Extend:

1. Can you apply unit test to this application? 
-> I tested on simulator. It's OK
With real device, sometime, Web broswer cannot access to https://flickr.com or https://m.flickr.com because of not safe connection(SSH)

2. How long it would take you to implement an addition upload service

such as Picasa to this app?
-> It's about 2-3 days. 1 days for investigate API and the remaining for coding (base on currect source code and layout) 
