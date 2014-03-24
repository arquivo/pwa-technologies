
"""InstallPWA.py: Downloads PWA4 Test collection from arhive.org .
More information related to this test collection can be found here:
M. Costa and M. J. Silva. Evaluating web archive search systems. In X. S. Wang, I. F. Cruz, A. Delis,and G. Huang, editors, WISE, volume 7651 of Lecture
Notes in Computer Science. Springer, 2012.

Two files 'itemsForAWP4firstBlock.txt' and 'itemsForAWP4secondBlock.txt' provides list of arc files to download 
"""

__author__ = "Zeynep Pehlivan"
__copyright__ = "Copyright 2014, Zeynep Pehlivan"
__license__ = "GPL"
__version__ = "1.0.0"
__email__ = "zeynep.pehlivan@lip6.fr"


import csv
import urllib
import multiprocessing
import itertools
import os.path


# update according to path that you would like to save PWA collection
pathtosave = "/data/pehlivanz/PWA4/"
archiveurl = "https://archive.org/download/"


def DownloadOneByOne(url,name):
	if not (os.path.exists(pathtosave + name)): # for extra launch, if there is connection error or something like this not to download the file already downloaded
		urllib.urlretrieve (url, pathtosave + name)




def worker(row):
	try:
		url = archiveurl + row[0] + "/" + row[1]
		name = row[1]
		DownloadOneByOne(url,name)
	except Exception as e:
		print e
		print(url)



def GetAll(infile):
	pool = multiprocessing.Pool(multiprocessing.cpu_count())
	results = []
	f = open(infile)
	reader = csv.reader(f,delimiter=" ")
	chunks = itertools.islice(reader,0, None)
	pool.map(worker, chunks)
	pool.close()
	pool.join()
	
if __name__ == '__main__':
# I put input files at in the output folder (pathtosave), if it is not the case for you, please update file paths here
	crawlBlock1 = pathtosave + 'itemsForAWP4firstBlock.txt'
	GetAll(crawlBlock1)
	crawlBlock1 = pathtosave + 'itemsForAWP4secondBlock.txt'
	GetAll(crawlBlock1)


