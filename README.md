## About

Vectors (Video communication through opportunistic relays and scalable video coding) is a Disruption Tolerant Networks (DTNs) research project developed at BITS Pilani, Hyderabad Campus.

In this project, we aim to create DTN using Android smartphones and tablets. We chose The Android platform for its ease of development and deployment. Moreover, most Android devices are equipped with several wireless radios such as Bluetooth, Wi-Fi, and GPS. This gives us an ideal base for collecting DTN trace data. In Vectors, we used the [Android Nearby Connections](https://developers.google.com/nearby/) API for autonomously connecting the devices using a combination of Bluetooth and Wi-Fi.

This DTN of Android devices is used for transferring [SHVC](https://mpeg.chiariglione.org/sites/default/files/files/standards/parts/docs/HEVC%20and%20Layered%20HEVC%20for%20UHD%20deployments.pdf) encoded video. SHVC is a special type of video compression algorithm which splits a video into multiple temporal and spatial layers. While the base video layers are necessarily required for playing the video, the higher layers are only required for increasing the video quality. This makes it amenable for transferring using a DTN as the base layer can be transferred first with the higher, quality enhancing layers transferred opportunistically if the number of contacts between DTN nodes increase.

For more details about the setup, installation, and recommended hardware, please refer to the [Wiki Page](https://github.com/swifiic/Vectors/wiki).
