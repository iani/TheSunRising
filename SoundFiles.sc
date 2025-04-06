//: Load sound files into buffers, creat a sound file view
// and a menu to select a file and display it.

SoundFiles {
	classvar default;
	var audioFileDir, audioBuffers, window;

	*autoStart {
		ServerBoot add: {
			default = this.new;
			default.audioBuffers;
			{ default.soundFileView; }.defer(1);
		};
		ServerQuit add: { this.default.close };
	}

	*default {
		^default ?? { default = this.new };
	}

	close { window !? { window.close; } }

	audioFileDir {
		^audioFileDir ?? { this.defaultAudioDir }
	}

	defaultAudioDir { ^PathName("~/TsrAudioFiles").fullPath; }

	audioBuffers {
		if (audioBuffers.isNil) {
			"I will load audio buffers".postln;
			this.loadAudioBuffers;
			"Loading audiobuffers. Rerun audioBuffers to update result";
		}{
			// "Audio buffers already exist".postln;
		};
		^audioBuffers;
	}

	loadAudioBuffers {
		var paths, buffers;
		"Loading audiobuffers METHOD. Rerun audioBuffers to update result".postln;
		audioBuffers = ();
		Server.default.waitForBoot({
			// Load files ending in wav, WAV, aiff, aif, AIFF, AIF"
			paths = (this.audioFileDir +/+ "*.wav").pathMatch;
			paths = paths ++ (this.audioFileDir +/+ "*.WAV").pathMatch;
			paths = paths ++ (this.audioFileDir +/+ "*.aiff").pathMatch;
			paths = paths ++ (this.audioFileDir +/+ "*.AIFF").pathMatch;
			paths = paths ++ (this.audioFileDir +/+ "*.aif").pathMatch;
			paths = paths ++ (this.audioFileDir +/+ "*.AIF").pathMatch;
			postln("audiofiledir is:" + audioFileDir);
			postln("Paths found are:" + paths);
			buffers = paths.postln collect: { | path |
				Buffer.read(Server.default, path);
			};
			buffers.postln;
			buffers do: { | buf |
				audioBuffers[
					PathName(buf.path.postln).fileNameWithoutExtension.asSymbol
				] = buf;
			};

		})
	}

	soundFileView {
		var sfview, soundfile;
		window = this.vlayoutRect(
			Rect(0, 0, 800, 400),
			PopUpMenu()
			.items_(audioBuffers.keys.asArray.sort)
			.action_({ | m |
				// m.value.postln;
				m.item.postln;
				soundfile = SoundFile();
				sfview.soundfile = soundfile;
				soundfile.openRead(audioBuffers[m.item.asSymbol].path);
				sfview.read(0, soundfile.numFrames);
			}),
			sfview = SoundFileView().soundfile_(
				soundfile = SoundFile();
			)
		);

		{
			var path;
			path = audioBuffers[audioBuffers.keys.asArray.sort.first].path;
			postln("Loading: " + path);
			soundfile.openRead(path);
			sfview.read(0, soundfile.numFrames);
		}.defer(1);
	}
}
