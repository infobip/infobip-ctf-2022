// Code generated by fileb0x at "2021-11-28 16:39:30.93360924 +0100 CET m=+0.003383215" from config file "b0x.yaml" DO NOT EDIT.
// modification hash(ffe6ae5a821367010d0ddbbe57673502.76aeabb7bb012cd748e951c53d192189)

package static


import (
  "bytes"
  "compress/gzip"
  "context"
  "io"
  "net/http"
  "os"
  "path"


  "golang.org/x/net/webdav"


)

var ( 
  // CTX is a context for webdav vfs
  CTX = context.Background()

  
  // FS is a virtual memory file system
  FS = webdav.NewMemFS()
  

  // Handler is used to server files through a http handler
  Handler *webdav.Handler

  // HTTP is the http file system
  HTTP http.FileSystem = new(HTTPFS)
)

// HTTPFS implements http.FileSystem
type HTTPFS struct {
	// Prefix allows to limit the path of all requests. F.e. a prefix "css" would allow only calls to /css/*
	Prefix string
}



// FileClientCrt is "client.crt"
var FileClientCrt = []byte("\x1f\x8b\x08\x00\x00\x00\x00\x00\x00\xff\x64\x92\xbb\x76\xa3\x3a\x18\x46\x7b\x9e\xe2\xf4\x59\x67\x59\x96\x4d\x82\x8b\x29\x24\x21\x90\x00\x61\x0b\x64\x6e\x5d\x6c\x63\xc0\x40\x42\x0c\x1e\x2e\x4f\x3f\x6b\x9c\x6a\x66\xfe\x72\xef\xe2\x2f\xbe\xfd\xff\xef\xc3\xd4\xe6\xfe\x7f\x84\x06\x8a\x5b\x9c\x20\x45\x9f\x54\x13\x9c\xe3\x5e\x11\x82\x54\x4a\x50\x71\xf8\x14\xe8\xd3\x26\xe4\xcb\x0e\xc5\x76\x87\x91\x20\x82\x1e\x27\xb2\x20\x07\x17\x7e\x84\x51\xaa\x50\x6d\x45\x22\x10\x23\x95\xa9\x19\x49\xc9\x35\x13\x75\xea\x04\xd7\x8d\x17\xf9\x20\x4d\x82\x46\x84\x74\x64\xe3\x53\xba\x26\x2e\x9d\xd3\x47\xd0\x9c\x5b\xbd\xb9\x10\x9c\xbc\xc7\xc1\xc7\x7b\x12\x2c\xdc\xc2\x20\x0f\xb1\xb8\xd8\x72\xd4\x58\x79\xf6\xc5\x8d\x4e\x42\xf1\xad\x50\xc7\x59\x2c\x62\x13\x3f\x19\xff\x93\xdd\xb0\x25\x64\x3f\x92\xef\xcf\x36\x1d\x1d\x1c\x29\xaa\x34\x81\xa9\x8d\xd6\x47\x4a\xd0\xe8\x1e\xe1\x6e\xc8\xc2\xb5\xba\xd8\x16\xc8\x14\x2d\x05\x36\xbe\x5d\x31\xa6\x61\xac\x83\x2c\x71\x1e\x59\x22\x8b\x08\x36\x75\x06\x1b\x70\x9e\xb1\xd4\x2e\xac\x2e\x14\x0b\x6a\xc1\xd2\x91\xa2\x94\xb9\x9f\x19\x5f\x6e\x80\x20\x99\x5a\xee\x96\x52\x44\xb8\x99\x16\x88\xd2\xd2\xe8\xd6\x8d\x77\x50\x32\xac\x1c\xb4\x6a\x4d\x40\xa4\xd9\x75\x86\x36\x4c\x70\xa8\x02\xc8\x09\xeb\x3a\xef\x9e\x64\x97\x70\xd9\x75\x83\x4e\x8c\xfe\xf4\xb6\x24\x2b\x78\xba\xbe\xe5\xa6\xd9\x57\x6c\x53\x4b\xc8\x09\xdc\xdf\xfb\xec\x3a\xfc\x4c\x45\xcc\x42\xeb\xbc\xce\x35\xe0\xb4\xf4\xea\x0f\x4b\x9b\xcc\x3a\x78\x10\xb2\x03\x04\x4e\x5d\x05\xe2\xd7\x3d\x70\x0d\x18\xcd\xd2\x3b\x7b\x5f\x27\xfa\xf7\x3a\x08\xd6\x48\xd8\x29\xd1\x84\x24\x75\x54\xae\x76\x25\xaf\xdc\xb2\x7e\x71\x44\x29\x9a\x91\x4d\xf7\x7d\xd3\xb7\x5b\xe8\xf5\x53\x95\x7f\xd0\x8a\x2f\xc9\x61\x33\x1f\x6e\x8f\x99\xeb\x4d\xe6\xb8\x56\xc4\x2e\xc6\xee\xe4\xb4\x50\x1b\xa6\x2d\x11\xd2\xac\xe0\xf5\xab\x20\x4d\xfc\x96\x3f\x46\xe5\xf4\x2f\x7e\xe8\x3b\x7c\x76\xa2\x63\xbb\x4a\x8e\xe5\x60\x03\xdb\x7a\x95\xeb\xd6\x7e\x61\xe7\x04\xc5\xb9\xc7\x54\xbb\x89\x5c\xac\xef\x35\xaa\x1f\xb2\x7d\x6c\xfc\xd0\x9e\x5d\x51\xdf\xfc\xb7\xb5\x5f\x01\x00\x00\xff\xff\x40\x56\xc7\x47\x88\x02\x00\x00")

// FileServerCrt is "server.crt"
var FileServerCrt = []byte("\x1f\x8b\x08\x00\x00\x00\x00\x00\x00\xff\x64\x92\x4f\x77\xa2\x30\x14\x47\xf7\x7c\x8a\xd9\xf7\xcc\x21\x50\xad\x65\xd1\xc5\x4b\x08\x21\x48\x70\x02\xe1\x4f\xd8\xa1\x28\x2a\xf8\xaf\xd5\xa6\xf2\xe9\xe7\x8c\x5d\xcd\xcc\x5b\xde\xbb\x78\x8b\xdf\xfd\xf9\xe7\x30\x65\x3c\xf9\x41\x68\xaa\x78\xc0\x09\x28\xfa\xa0\x96\xe0\x1c\x7f\xf8\x84\x80\xd2\x04\xba\x5f\x27\x01\x27\x46\xc8\x85\x65\x62\xe2\x61\x10\x44\xd0\xfc\x8b\x8c\x10\xe1\x2e\x29\x30\x68\x05\x7d\x50\x88\x54\x18\x2a\xb5\x5f\x48\xc9\x2d\x1f\xce\x6a\xe9\x3a\x43\x5c\x24\x48\x57\xe9\x20\x32\x6a\x42\xf3\x90\x73\x1f\x6f\xa3\xe5\x31\x1d\x56\x87\xe9\xd0\x12\x5c\x35\x65\x7a\x6c\xaa\x74\xe4\x01\x46\xeb\x0c\x8b\x96\x49\x63\x85\xdb\x55\x22\xf6\xf4\x4b\x28\x3e\x11\x2a\xbf\x8b\x91\x7f\x95\x0f\xc6\xff\x66\x7b\x1c\x08\xf9\x61\xc8\xf7\x67\x46\x4d\x84\x0b\x45\x95\x25\x30\x65\xe0\xe4\x94\x80\x99\xe7\xae\x77\xad\x33\x47\xb5\x2c\x40\xb5\xa2\x5b\x81\x5f\xbf\x5d\x67\x74\x56\x4e\x51\x5d\x45\xb7\xba\x92\x5d\xe1\x0e\x7d\xed\x0e\x68\x75\xc7\xd2\x6a\xc3\xbe\x53\x61\xda\x8b\x50\x1b\x0a\x3a\x9c\x9f\x6a\x3e\xee\x11\x01\xa9\x83\xf9\x84\x52\x20\xdc\xd7\x1d\xd0\x69\x33\xf3\xc2\xb0\xf4\x6a\xfb\xb2\x73\x16\xe3\xf2\x7e\x26\x6b\xa3\x1c\x62\x0d\xf7\x52\xcc\x9e\x8f\x65\x5c\x08\xb6\x59\x31\xba\x74\x9a\x6e\x50\x36\x2a\x83\xcb\x44\x37\x63\x00\xd1\xf5\x5d\xee\x44\xde\x1d\x7d\x3d\x25\x2c\x96\x49\x9b\x3a\xb0\x79\x06\xea\x41\x6c\x9f\x06\xab\xff\x3c\x2f\x36\xd1\x75\xed\xad\x3f\x5f\xe9\xe6\x70\x01\x13\xde\x0e\xea\x9e\x2e\x17\x3c\x4e\x2e\x99\x7e\xb1\xaf\x78\xb4\x53\xfd\xef\x3a\xe0\x76\x20\x58\x4e\x2c\x21\x89\x46\x43\x35\x45\x9b\x97\x31\xb6\xbd\x58\xcd\xca\xb8\xc0\x7e\x48\x03\xb3\x88\x5e\x9a\x5d\xe5\x84\x43\x2c\xec\xf3\xd6\xb4\x71\x34\xad\x71\x36\xa1\xed\xb3\xb3\x30\xf1\x6d\xea\xd6\x2e\xe4\x56\x92\x19\x22\x30\x9a\xdd\x5e\xd7\xe0\xe3\xa0\x67\x72\x7e\xde\x3d\xad\xc4\x2a\x65\xac\xcf\x23\x2f\xbc\xa5\x33\xce\xf1\xbb\x2d\xbb\x04\xed\xd5\x70\x98\x77\xf8\x89\x43\x46\x7c\xd3\x28\x84\x0a\xeb\xe8\xc2\xb8\xef\xde\xde\xac\x47\x57\x34\xf1\xff\x6f\xed\x77\x00\x00\x00\xff\xff\xdd\xef\x1c\x5b\x88\x02\x00\x00")

// FileServerKey is "server.key"
var FileServerKey = []byte("\x1f\x8b\x08\x00\x00\x00\x00\x00\x00\xff\x6c\x90\x31\x77\x82\x30\x18\x00\x77\x7e\x85\x7b\x5e\x9f\xc2\x43\x2d\x43\x87\x2f\x90\x60\x40\x03\x8d\x80\x8d\x5b\x11\x05\x04\x5b\x41\x4c\xb0\xbf\xbe\xaf\xce\xbd\xf5\xa6\xbb\x97\x3f\x30\xf1\x19\x9f\x10\x77\x12\x0b\x96\x41\x42\x26\x21\x91\x4f\x61\x6c\x98\xdf\x40\x49\x30\xf6\x80\xd6\xc5\xa0\xce\x8e\x1f\x89\x2c\x11\x63\xd4\xe5\xef\x3c\x2a\x40\xaf\xef\x57\x1d\xa0\x53\x9e\xb5\x76\xa7\x4f\xd8\xb2\xf3\x6a\x4e\x2b\x6f\x7c\x08\x4e\xfd\xc8\x28\x1e\x8b\xae\x75\x8e\xf5\x9d\x59\x25\xd6\x92\x86\x36\x21\xe0\x86\xd5\x1e\x78\x0d\x90\xb4\xbd\x9a\x1d\x8a\xdc\xfc\x42\xdd\x3a\x5d\x72\x15\x36\xc1\x12\xc7\x69\xf8\x11\xc8\x1f\x75\x2c\xe4\x60\xa4\x7a\xef\x68\x29\x54\xd6\xb9\x59\x3c\x15\x72\xd7\x57\x55\xbf\x49\xe1\x62\x0d\x2e\x1b\xb7\xee\x81\x9f\x1b\x26\x07\x30\xcd\x55\x8a\xa7\x07\x48\x66\xa0\x50\xbd\xdb\xa2\x66\xfe\x7a\x31\x97\xa6\xb1\x98\x6a\x81\x3e\xbf\x3d\x38\x2e\xf6\x71\x40\x6f\x76\x79\xb3\xae\xc1\xb9\x47\xb3\x55\xec\xd0\xf2\xcd\x78\x96\x12\xee\xfd\x3b\xe0\x37\x00\x00\xff\xff\x93\x57\x90\x9a\x20\x01\x00\x00")



func init() {
  err := CTX.Err()
  if err != nil {
		panic(err)
	}









  
  var f webdav.File
  

  
  
  var rb *bytes.Reader
  var r *gzip.Reader
  
  

  
  
  
  rb = bytes.NewReader(FileClientCrt)
  r, err = gzip.NewReader(rb)
  if err != nil {
    panic(err)
  }

  err = r.Close()
  if err != nil {
    panic(err)
  }
  
  

  f, err = FS.OpenFile(CTX, "client.crt", os.O_RDWR|os.O_CREATE|os.O_TRUNC, 0777)
  if err != nil {
    panic(err)
  }

  
  
  _, err = io.Copy(f, r)
  if err != nil {
    panic(err)
  }
  
  

  err = f.Close()
  if err != nil {
    panic(err)
  }
  
  
  
  rb = bytes.NewReader(FileServerCrt)
  r, err = gzip.NewReader(rb)
  if err != nil {
    panic(err)
  }

  err = r.Close()
  if err != nil {
    panic(err)
  }
  
  

  f, err = FS.OpenFile(CTX, "server.crt", os.O_RDWR|os.O_CREATE|os.O_TRUNC, 0777)
  if err != nil {
    panic(err)
  }

  
  
  _, err = io.Copy(f, r)
  if err != nil {
    panic(err)
  }
  
  

  err = f.Close()
  if err != nil {
    panic(err)
  }
  
  
  
  rb = bytes.NewReader(FileServerKey)
  r, err = gzip.NewReader(rb)
  if err != nil {
    panic(err)
  }

  err = r.Close()
  if err != nil {
    panic(err)
  }
  
  

  f, err = FS.OpenFile(CTX, "server.key", os.O_RDWR|os.O_CREATE|os.O_TRUNC, 0777)
  if err != nil {
    panic(err)
  }

  
  
  _, err = io.Copy(f, r)
  if err != nil {
    panic(err)
  }
  
  

  err = f.Close()
  if err != nil {
    panic(err)
  }
  


  Handler = &webdav.Handler{
    FileSystem: FS,
    LockSystem: webdav.NewMemLS(),
  }


}



// Open a file
func (hfs *HTTPFS) Open(path string) (http.File, error) {
  path = hfs.Prefix + path


  f, err := FS.OpenFile(CTX, path, os.O_RDONLY, 0644)
  if err != nil {
    return nil, err
  }

  return f, nil
}

// ReadFile is adapTed from ioutil
func ReadFile(path string) ([]byte, error) {
  f, err := FS.OpenFile(CTX, path, os.O_RDONLY, 0644)
  if err != nil {
    return nil, err
  }

  buf := bytes.NewBuffer(make([]byte, 0, bytes.MinRead))

  // If the buffer overflows, we will get bytes.ErrTooLarge.
  // Return that as an error. Any other panic remains.
  defer func() {
    e := recover()
    if e == nil {
      return
    }
    if panicErr, ok := e.(error); ok && panicErr == bytes.ErrTooLarge {
      err = panicErr
    } else {
      panic(e)
    }
  }()
  _, err = buf.ReadFrom(f)
  return buf.Bytes(), err
}

// WriteFile is adapTed from ioutil
func WriteFile(filename string, data []byte, perm os.FileMode) error {
  f, err := FS.OpenFile(CTX, filename, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, perm)
  if err != nil {
    return err
  }
  n, err := f.Write(data)
  if err == nil && n < len(data) {
    err = io.ErrShortWrite
  }
  if err1 := f.Close(); err == nil {
    err = err1
  }
  return err
}

// WalkDirs looks for files in the given dir and returns a list of files in it
// usage for all files in the b0x: WalkDirs("", false)
func WalkDirs(name string, includeDirsInList bool, files ...string) ([]string, error) {
	f, err := FS.OpenFile(CTX, name, os.O_RDONLY, 0)
	if err != nil {
		return nil, err
	}

	fileInfos, err := f.Readdir(0)
	if err != nil {
    return nil, err
  }
  
  err = f.Close()
  if err != nil {
		return nil, err
	}

	for _, info := range fileInfos {
		filename := path.Join(name, info.Name())

		if includeDirsInList || !info.IsDir() {
			files = append(files, filename)
		}

		if info.IsDir() {
			files, err = WalkDirs(filename, includeDirsInList, files...)
			if err != nil {
				return nil, err
			}
		}
	}

	return files, nil
}


